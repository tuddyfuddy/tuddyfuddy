package com.heejuk.tuddyfuddy.contextservice.service;

import static com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.TO_GRID;
import static com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.convertGRID_GPS;

import com.fasterxml.jackson.databind.JsonNode;
import com.heejuk.tuddyfuddy.contextservice.client.WeatherApiClient;
import com.heejuk.tuddyfuddy.contextservice.entity.Weather;
import com.heejuk.tuddyfuddy.contextservice.repository.WeatherRepository;
import com.heejuk.tuddyfuddy.contextservice.util.GridGpsUtil.LatXLngY;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final WeatherApiClient weatherApiClient;
    private final RedisTemplate<String, Weather> redisTemplate;

    private static final long CACHE_TTL_MINUTES = 70; // 기상청 API 업데이트 주기에 맞춤

    public Weather getWeatherByLocation(
        Double latitude,
        Double longitude
    ) {
        LatXLngY latXLngY = convertGRID_GPS(TO_GRID,
                                            latitude,
                                            longitude);
        int x = (int) latXLngY.x;
        int y = (int) latXLngY.y;
        // 캐시된 날씨 데이터 확인
        Optional<Weather> cachedWeather = weatherRepository
            .findFirstByXAndYOrderByTimestampDesc(x,
                                                  y);

        String redisKey = generateWeatherKey(x,
                                             y);

        if (cachedWeather.isPresent() && isWeatherDataFresh(cachedWeather.get())) {
            return cachedWeather.get();
        }

        // 새로운 날씨 데이터 조회
        JsonNode newWeather = weatherApiClient.fetchWeatherData(x,
                                                                y)
                                              .block();
        if (newWeather == null) {
            // 에러 또는 데이터 없음 처리
            return null;
        }

        // newWeather 에서 필요한 필드 추출
        Weather weather = parseJsonToWeather(newWeather,
                                             x,
                                             y);

        // 데이터베이스에 저장
        return weatherRepository.save(weather);
    }

    private String generateWeatherKey(
        int x,
        int y
    ) {
        return x + " " + y;
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


    private boolean isWeatherDataFresh(Weather weather) {
        return Duration.between(weather.getTimestamp(),
                                LocalDateTime.now())
                       .toMinutes() < 70;
    }


}
