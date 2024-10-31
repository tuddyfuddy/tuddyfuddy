package com.heejuk.tuddyfuddy.contextservice.service;

import static com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.TO_GRID;
import static com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.convertGRID_GPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.contextservice.client.WeatherApiClient;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherResponse;
import com.heejuk.tuddyfuddy.contextservice.entity.Weather;
import com.heejuk.tuddyfuddy.contextservice.mapper.WeatherMapper;
import com.heejuk.tuddyfuddy.contextservice.repository.WeatherRepository;
import com.heejuk.tuddyfuddy.contextservice.util.FormatUtil;
import com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.LatXLngY;
import feign.FeignException;
import java.time.Duration;
import java.time.LocalDateTime;
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

    private final WeatherMapper weatherMapper;
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
            // api 에서 불러오기
            JsonNode newWeather = weatherApiClient.fetchWeatherData(
                apiKey,
                1,
                1000,
                "JSON",
                FormatUtil.formatDateKST("yyyyMMdd"),
                FormatUtil.formatTimeKST("HHmm"),
                x,
                y
            );

            Weather weather = parseJsonToWeather(newWeather,
                                                 x,
                                                 y);
            // 저장할 필요가 아직 없음
//            weatherRepository.save(weather);
            WeatherResponse response = weatherMapper.toDto(weather);

            try {
                String jsonValue = objectMapper.writeValueAsString(response);
                redisTemplate.opsForValue()
                             .set(redisKey,
                                  jsonValue,
                                  Duration.ofMinutes(CACHE_TTL_MINUTES));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize weather data",
                          e);
            }

            return response;

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

    // JSON 데이터를 Weather 엔티티로 변환
    private Weather parseJsonToWeather(
        JsonNode json,
        Integer x,
        Integer y
    ) {
        JsonNode items = json.path("response")
                             .path("body")
                             .path("items")
                             .path("item");

        String weatherDescription = null;
        Double temperature = null;
        Double humidity = null;

        for (JsonNode item : items) {
            String category = item.path("category")
                                  .asText();
            String value = item.path("obsrValue")
                               .asText();

            // x, y 좌표 추출 (nx와 ny 값은 모든 항목에서 동일하므로 한 번만 설정)
            if (x == null && y == null) {
                x = item.path("nx")
                        .asInt();
                y = item.path("ny")
                        .asInt();
            }

            switch (category) {
                case "T1H": // 기온
                    temperature = Double.parseDouble(value);
                    break;
                case "REH": // 습도
                    humidity = Double.parseDouble(value);
                    break;
                case "PTY": // 강수 형태
                    if ("0".equals(value)) {
                        weatherDescription = "맑음";
                    } else if ("1".equals(value)) {
                        weatherDescription = "비";
                    } else if ("2".equals(value)) {
                        weatherDescription = "비/눈";
                    } else if ("3".equals(value)) {
                        weatherDescription = "눈";
                    }
                    break;
                case "SKY": // 하늘 상태
                    if (weatherDescription == null) { // PTY가 없을 때만 하늘 상태를 사용
                        if ("1".equals(value)) {
                            weatherDescription = "맑음";
                        } else if ("3".equals(value)) {
                            weatherDescription = "구름많음";
                        } else if ("4".equals(value)) {
                            weatherDescription = "흐림";
                        }
                    }
                    break;
            }
        }

        // Weather 엔티티 빌더 사용하여 설정된 값으로 객체 생성
        return Weather.builder()
                      .x(x)
                      .y(y)
                      .timestamp(LocalDateTime.now())
                      .temperature(temperature)
                      .humidity(humidity)
                      .weather(weatherDescription)
                      .createdAt(LocalDateTime.now())
                      .build();
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
