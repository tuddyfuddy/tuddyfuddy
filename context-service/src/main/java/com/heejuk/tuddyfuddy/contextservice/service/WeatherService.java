package com.heejuk.tuddyfuddy.contextservice.service;

import static com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.TO_GRID;
import static com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.convertGRID_GPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.contextservice.client.WeatherApiClient;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherListResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherResponse;
import com.heejuk.tuddyfuddy.contextservice.exception.WeatherApiRequestException;
import com.heejuk.tuddyfuddy.contextservice.repository.WeatherRepository;
import com.heejuk.tuddyfuddy.contextservice.util.FormatUtil;
import com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.LatXLngY;
import com.heejuk.tuddyfuddy.contextservice.util.ParseUtil;
import feign.FeignException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService {

    private static final Duration CACHE_TTL = Duration.ofHours(48); // 이틀
    private static final String BASE_TIME = "0500";

    private final WeatherRepository weatherRepository;
    private final WeatherApiClient weatherApiClient;
    private final ParseUtil parseUtil;

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherListResponse getWeathersByLocation(
        String latitude,
        String longitude
    ) {
        LatXLngY latXLngY = convertGRID_GPS(TO_GRID,
                                            Double.parseDouble(latitude),
                                            Double.parseDouble(longitude));
        int x = (int) latXLngY.x;
        int y = (int) latXLngY.y;

        String today = FormatUtil.formatDateKST("yyyyMMdd");
        String yesterday = FormatUtil.formatPreviousDateKST("yyyyMMdd");
        
        // 오늘, 어제 날씨데이터
        WeatherResponse todayWeather = null;
        WeatherResponse yesterdayWeather = null;
        try {
            todayWeather = getDailyWeatherByLocation(x, y, today);
        } catch (WeatherApiRequestException e) {
            log.error(
                "Failed to fetch today weather data for location x:{}, y:{} due to API request failure",
                x, y);
        }
        try {
            yesterdayWeather = getDailyWeatherByLocation(x, y, yesterday);
        } catch (WeatherApiRequestException e) {
            log.error(
                "Failed to fetch yesterday weather data for location x:{}, y:{} due to API request failure",
                x, y);
        }

        // 반환
        return WeatherListResponse.builder()
                                  .todayWeather(todayWeather)
                                  .yesterdayWeather(yesterdayWeather)
                                  .build();
    }

    private WeatherResponse getDailyWeatherByLocation(
        int x,
        int y,
        String date
    ) {
        String redisKey = generateWeatherKey(x, y, date);
        String cachedJson = redisTemplate.opsForValue()
                                         .get(redisKey);

        // 캐시 확인
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson,
                                              WeatherResponse.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse cached weather data",
                          e);
                redisTemplate.delete(redisKey);  // 잘못된 캐시 데이터 삭제
            }
        }

        // 기상청 API 조회
        try {
            JsonNode curWeatherData = weatherApiClient.fetchWeatherData(
                apiKey,
                1,
                300,
                "JSON",
                date,
                BASE_TIME,
                x,
                y
            );

            WeatherResponse curWeather = parseJsonToWeather(curWeatherData, x, y, date);
            try {
                String jsonValue = objectMapper.writeValueAsString(curWeather);
                redisTemplate.opsForValue()
                             .set(redisKey,
                                  jsonValue,
                                  CACHE_TTL);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize weather data",
                          e);
            }

            return curWeather;

        } catch (FeignException e) {
            log.error("Failed to fetch weather data at x:{}, y:{}, error: {}",
                      x,
                      y,
                      e.getMessage());
            throw new WeatherApiRequestException("Failed to fetch weather data");
        }
    }

    /**
     * REDIS 키 만들기
     *
     * @param x
     * @param y
     * @param date
     * @return
     */
    private String generateWeatherKey(
        int x,
        int y,
        String date
    ) {
        return String.format("weather:%s:%d:%d",
                             date,
                             x,
                             y);
    }

    // JSON 데이터를 WeatherResponse Dto 로 변환
    private WeatherResponse parseJsonToWeather(
        JsonNode json,
        Integer x,
        Integer y,
        String queryDate
    ) {
        JsonNode header = json.path("response")
                              .path("header");

        String resultCode = header.path("resultCode")
                                  .asText();
        String resultMsg = header.path("resultMsg")
                                 .asText();

        // 기상청 API 요청 에러 처리
        if (!resultCode.equals("00")) {
            throw new WeatherApiRequestException(
                "기상청 API 요청 에러 코드[" + resultCode + "] - " + resultMsg + " - x: " + x + " y: " + y);
        }

        JsonNode items = json.path("response")
                             .path("body")
                             .path("items")
                             .path("item");

        // weather 저장
        Map<String, String> weatherInfo = new HashMap<>();
        // 당일은 max, min 안 줘서 직접 계산. 다음날부터 제공
        double maxTemperature = -1000D;
        double minTemperature = 1000D;

        for (JsonNode item : items) {
            String category = item.path("category")
                                  .asText();
            String date = item.path("fcstDate")
                              .asText();

            // 다른 날이면 break
            if (!date.equals(queryDate)) {
                break;
            }

            String time = item.path("fcstTime")
                              .asText();
            String value = item.path("fcstValue")
                               .asText();

            Double temperature = null;

            // x, y 좌표 추출 (nx와 ny 값은 모든 항목에서 동일하므로 한 번만 설정)
            if (x == null && y == null) {
                x = item.path("nx")
                        .asInt();
                y = item.path("ny")
                        .asInt();
            }

            switch (category) {
                case "TMP": // 기온
                    temperature = Double.parseDouble(value);
                    maxTemperature = Math.max(maxTemperature, temperature);
                    minTemperature = Math.min(minTemperature, temperature);
                    break;
                case "PTY": // 강수 형태
                    if ("0".equals(value)) {
                        weatherInfo.put(time, weatherInfo.getOrDefault(time, "") + "맑음");
                    } else if ("1".equals(value)) {
                        weatherInfo.put(time, weatherInfo.getOrDefault(time, "") + "비");
                    } else if ("2".equals(value)) {
                        weatherInfo.put(time, weatherInfo.getOrDefault(time, "") + "비/눈");
                    } else if ("3".equals(value)) {
                        weatherInfo.put(time, weatherInfo.getOrDefault(time, "") + "눈");
                    }
                    break;
                case "SKY": // 하늘 상태
                    if (weatherInfo.containsKey(time)) { // PTY가 없을 때만 하늘 상태를 사용
                        if ("1".equals(value)) {
                            weatherInfo.put(time, "맑음");
                        } else if ("3".equals(value)) {
                            weatherInfo.put(time, "구름많음");
                        } else if ("4".equals(value)) {
                            weatherInfo.put(time, "흐림");
                        }
                    }
                    break;
            }
        }

        String weather = getWeatherFromInfo(weatherInfo);
        String note = getNoteFromInfo(weatherInfo);

        // WeatherResponse 빌더 사용하여 설정된 값으로 객체 생성
        return WeatherResponse.builder()
                              .x(x)
                              .y(y)
                              .timestamp(LocalDateTime.now())
                              .minTemperature(minTemperature)
                              .maxTemperature(maxTemperature)
                              .weather(weather)
                              .note(note)
                              .createdAt(LocalDateTime.now())
                              .build();
    }

    private String getWeatherFromInfo(Map<String, String> weatherInfo) {
        // 가장 많이 나타난 날씨를 대표 날씨로 선정
        Map<String, Integer> weatherCount = new HashMap<>();

        for (String weather : weatherInfo.values()) {
            weatherCount.put(weather, weatherCount.getOrDefault(weather, 0) + 1);
        }

        return weatherCount.entrySet()
                           .stream()
                           .max(Map.Entry.comparingByValue())
                           .map(Map.Entry::getKey)
                           .orElseThrow(() -> new RuntimeException("날씨 정보를 찾을 수 없습니다."));
    }

    private String getNoteFromInfo(Map<String, String> weatherInfo) {
        String mainWeather = getWeatherFromInfo(weatherInfo);
        List<String> significantChanges = new ArrayList<>();

        // 시간대를 오전/오후로 구분
        Map<String, List<String>> timeSlots = new HashMap<>();
        timeSlots.put("오전", new ArrayList<>());
        timeSlots.put("오후", new ArrayList<>());

        weatherInfo.forEach((time, weather) -> {
            int hour = Integer.parseInt(time.substring(0, 2));
            String slot = (hour < 12) ? "오전" : "오후";

            // 대표 날씨와 다르고, 비나 눈이 포함된 경우만 기록
            if (!weather.equals(mainWeather) &&
                (weather.contains("비") || weather.contains("눈"))) {
                timeSlots.get(slot)
                         .add(weather);
            }
        });

        // note 생성
        StringBuilder note = new StringBuilder();
        if (!timeSlots.get("오전")
                      .isEmpty()) {
            String morningWeather = getMostFrequent(timeSlots.get("오전"));
            note.append("오전에 ")
                .append(morningWeather);
        }

        if (!timeSlots.get("오후")
                      .isEmpty()) {
            if (note.length() > 0) {
                note.append(", ");
            }
            String afternoonWeather = getMostFrequent(timeSlots.get("오후"));
            note.append("오후에 ")
                .append(afternoonWeather);
        }

        return note.length() > 0 ? note.toString() : null;
    }

    private String getMostFrequent(List<String> list) {
        return list.stream()
                   .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                   .entrySet()
                   .stream()
                   .max(Map.Entry.comparingByValue())
                   .map(Map.Entry::getKey)
                   .orElse("");
    }

    @Async
    public void dailyFetchAll() {
        long startTime = System.currentTimeMillis();
        String today = FormatUtil.formatDateKST("yyyyMMdd");
        String resourcePath = "classpath:weather.csv";
        int skipLines = 1;

        AtomicInteger successCount = new AtomicInteger(0); // 성공한 카운트
        AtomicInteger failCount = new AtomicInteger(0); // 실패한 카운트

        log.info("Starting daily weather fetch for date: {}", today);

        // csv parsing 한 데이터
        List<String[]> locations = parseUtil.parseCsv(resourcePath, skipLines);

        // 병렬 처리를 위한 ExecutorService 설정
        int threadPoolSize = Runtime.getRuntime()
                                    .availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        // CompletableFuture : 비동기 연산을 위한 인터페이스
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        try {
            for (String[] location : locations) {
                // [
                // 0: kor,
                // 1: 1111051500,
                // 2: 서울특별시,
                // 3: 종로구,
                // 4: 청운효자동,
                // 5: 60, -> X
                // 6: 127, -> Y
                // 7: 126,
                // 8: 58,
                // 9: 14.35,
                // 10: 37,
                // 11: 35,
                // 12: 2.89,
                // 13: 126.9706519, -> longitude
                // 14: 37.5841367,  -> latitude
                // 15: ,
                // ]
                if (location[3].isEmpty() || location[4].isEmpty()) {
                    continue; // [3, 4] 는 비어있으면 패스
                }

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    String regionName = String.join(" ", location[2], location[3], location[4]);
                    try {
                        int x = Integer.parseInt(location[5]);
                        int y = Integer.parseInt(location[6]);
                        // x, y로 날씨 정보 가져오기
                        WeatherResponse weatherResponse = getDailyWeatherByLocation(
                            x, y, today
                        );
                        // 성공횟수 증가
                        successCount.incrementAndGet();
                        log.debug("Successfully fetched weather data for region: {}", regionName);
                    } catch (NumberFormatException e) { // number format 에러
                        failCount.incrementAndGet();
                        log.error("Invalid coordinate format for region {}: {}", regionName,
                                  e.getMessage());
                    } catch (WeatherApiRequestException e) { // weather api 호출 에러
                        failCount.incrementAndGet();
                        log.error("Weather API request failed for region {}: {}", regionName,
                                  e.getMessage());
                    } catch (Exception e) { // 그 외 에러
                        failCount.incrementAndGet();
                        log.error("Unexpected error while processing weather for region {}: {}",
                                  regionName, e.getMessage(), e);
                    }
                }, executorService);

                futures.add(future);
            }

            // 모든 작업 완료 대기
            // allOf() : 여러 개의 CompletableFuture를 하나로 결합
            // join(): 모든 작업이 완료될 때까지 현재 스레드를 블록
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                             .join();
        } finally {
            executorService.shutdown(); // 새 작업 execute 종료. 이미 execute 된 작업은 계속 실행
            try {
                // 안전한 종료 대기
                // awaitTermination: 주어진 시간 동안 모든 작업 완료 대기. 시간 초과 시 강제 종료
                // shutdownNow(): 실행 중인 작업들에 인터럽트 보냄
                if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                    log.warn("ExecutorService did not terminate in the specified time.");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) { // 종료 대기 중 인터럽트가 발생한 경우 처리
                // 강제로 모든 작업을 중단
                log.error("ExecutorService termination interrupted", e);
                executorService.shutdownNow();
                // 현재 스레드의 인터럽트 상태를 복원
                Thread.currentThread()
                      .interrupt();
            }
        }

        log.info("Daily weather fetch completed. Success: {}, Failed: {}, " +
                     "Total time taken: {} seconds",
                 successCount.get(),
                 failCount.get(),
                 (System.currentTimeMillis() - startTime) / 1000.0);

    }

    public void dailyFetchAllSequential() {
        long startTime = System.currentTimeMillis();
        String today = FormatUtil.formatDateKST("yyyyMMdd");
        String resourcePath = "classpath:weather.csv";

        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        List<String[]> locations = parseUtil.parseCsv(resourcePath, 1);

        for (String[] location : locations) {
            if (location[3].isEmpty() || location[4].isEmpty()) {
                skipCount++;
                continue;
            }

            try {
                int x = Integer.parseInt(location[5]);
                int y = Integer.parseInt(location[6]);
                getDailyWeatherByLocation(x, y, today);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("Error processing location: {}", e.getMessage());
            }
        }

        log.info("Sequential fetch completed. Success: {}, Failed: {}, Skipped: {}, " +
                     "Total time taken: {} seconds",
                 successCount, failCount, skipCount,
                 (System.currentTimeMillis() - startTime) / 1000.0);
    }
}
