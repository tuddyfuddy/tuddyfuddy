package com.heejuk.tuddyfuddy.contextservice.controller;

import com.heejuk.tuddyfuddy.contextservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherResponse;
import com.heejuk.tuddyfuddy.contextservice.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/context/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public CommonResponse<WeatherResponse> getWeatherData(
        @RequestParam("latitude") Double latitude,
        @RequestParam("longitude") Double longitude
    ) {
        return CommonResponse.ok("Weather data fetched successfully",
                                 weatherService.getWeatherByLocation(latitude,
                                                                     longitude));
    }
}
