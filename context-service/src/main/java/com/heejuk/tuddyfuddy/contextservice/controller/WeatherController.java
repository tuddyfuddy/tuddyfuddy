package com.heejuk.tuddyfuddy.contextservice.controller;

import com.heejuk.tuddyfuddy.contextservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherListResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherLocationResponse;
import com.heejuk.tuddyfuddy.contextservice.service.LocationService;
import com.heejuk.tuddyfuddy.contextservice.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/context/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final LocationService locationService;

    @GetMapping
    public CommonResponse<WeatherLocationResponse> getWeatherContextData(
        @RequestParam("latitude") String latitude,
        @RequestParam("longitude") String longitude
    ) {
        WeatherListResponse weathersByLocation = weatherService.getWeathersByLocation(latitude,
                                                                                      longitude);
        String location = locationService.getLocation(longitude, latitude);

        return CommonResponse.ok("Weather data fetched successfully",
                                 WeatherLocationResponse.builder()
                                                        .weathers(weathersByLocation)
                                                        .location(location)
                                                        .build()
        );
    }

    @PostMapping("/async")
    public void asdf1() {
        weatherService.dailyFetchAll();
    }

    @PostMapping("/sync")
    public void asdf2() {
        weatherService.dailyFetchAllSequential();
    }
}
