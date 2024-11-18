package com.heejuk.tuddyfuddy.contextservice.config;

import com.heejuk.tuddyfuddy.contextservice.service.WeatherService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final WeatherService weatherService;

    @Scheduled(cron = "${weather.fetch.cron}")
    public void fetchWeatherData() {
        log.info("Starting scheduled weather data fetch at {}", LocalDateTime.now());
        try {
            log.info("Fetching weather data from API...");
            weatherService.dailyFetchAll();
        } catch (Exception e) {
            try {
                log.error("Failed to fetch weather data", e);
                weatherService.dailyFetchAll();
            } catch (Exception e2) {
                log.error("Failed to fetch weather data again after failure", e2);
            }
        } finally {
            log.info("Finished scheduled weather data fetch at {}", LocalDateTime.now());
        }
    }
}
