package com.heejuk.tuddyfuddy.contextservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "weather-api", url = "${weather.api.url}")
public interface WeatherApiClient {

    @GetMapping("/VilageFcstInfoService_2.0/getVilageFcst")
    JsonNode fetchWeatherData(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("pageNo") int pageNo,
        @RequestParam("numOfRows") int numOfRows,
        @RequestParam("dataType") String dataType,
        @RequestParam("base_date") String baseDate,
        @RequestParam("base_time") String baseTime,
        @RequestParam("nx") Integer x,
        @RequestParam("ny") Integer y
    );
}