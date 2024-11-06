package com.heejuk.tuddyfuddy.contextservice.service;

import static com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.TO_GRID;
import static com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.convertGRID_GPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.contextservice.client.WeatherApiClient;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherResponse;
import com.heejuk.tuddyfuddy.contextservice.repository.WeatherRepository;
import com.heejuk.tuddyfuddy.contextservice.util.FormatUtil;
import com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.LatXLngY;
import feign.FeignException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final WeatherApiClient weatherApiClient;

    private final ObjectMapper objectMapper;

    @Value("${weather.api.key}")
    private String apiKey;

    private final RedisTemplate<String, String> redisTemplate;

    private static final long CACHE_TTL_MINUTES = 61; // 기상청 API 업데이트 주기에 맞춤

    public WeatherResponse getWeatherByLocation(
        Double latitude,
        Double longitude
    ) {
        LatXLngY latXLngY = convertGRID_GPS(TO_GRID,
                                            latitude,
                                            longitude);
        int x = (int) latXLngY.x;
        int y = (int) latXLngY.y;
        // 1. redis 키
        String redisKey = generateWeatherKey(x,
                                             y);
        // 1.1 redis 값
        String cachedJson = redisTemplate.opsForValue()
                                         .get(redisKey);

        // 2. redis 캐시 히트
        if (cachedJson != null) {
            try {
                WeatherResponse cachedWeather = objectMapper.readValue(cachedJson,
                                                                       WeatherResponse.class);
                if (isWeatherDataFresh(cachedWeather)) {
                    log.info("Cache hit for weather data at x:{}, y:{}",
                             x,
                             y);
                    return cachedWeather;
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to parse cached weather data",
                          e);
            }
        }

        try {
            String baseDate = FormatUtil.formatDateKST("yyyyMMdd");
            String baseTime = "0500";
            String previousDate = FormatUtil.formatPreviousDateKST("yyyyMMdd");
            // api 에서 불러오기
            JsonNode curWeatherData = weatherApiClient.fetchWeatherData(
                apiKey,
                1,
                300,
                "JSON",
                baseDate,
                baseTime,
                x,
                y
            );
            JsonNode prevWeatherData = weatherApiClient.fetchWeatherData(
                apiKey,
                1,
                300,
                "JSON",
                baseDate,
                baseTime,
                x,
                y
            );

            WeatherResponse curWeather = parseJsonToWeather(curWeatherData, x, y, baseDate);
            WeatherResponse prevWeather = parseJsonToWeather(prevWeatherData, x, y, previousDate);
            try {
                String jsonValue = objectMapper.writeValueAsString(curWeather);
                redisTemplate.opsForValue()
                             .set(redisKey,
                                  jsonValue,
                                  Duration.ofMinutes(CACHE_TTL_MINUTES));
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
            return null;
        }
    }

    /**
     * REDIS 키 만들기
     *
     * @param x
     * @param y
     * @return
     */
    private String generateWeatherKey(
        int x,
        int y
    ) {
        return String.format("weather:%d:%d",
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
            throw new RuntimeException("기상청 API 요청 에러 코드[" + resultCode + "] - " + resultMsg);
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
                    System.out.println("temperature = " + temperature);
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

    /**
     * 캐시된 값이 최근 값인지 여부
     *
     * @param weather
     * @return
     */
    private boolean isWeatherDataFresh(WeatherResponse weather) {
        return Duration.between(weather.timestamp(),
                                LocalDateTime.now())
                       .toMinutes() < CACHE_TTL_MINUTES;
    }


}
