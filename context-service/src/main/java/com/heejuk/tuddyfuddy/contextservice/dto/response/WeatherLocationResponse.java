package com.heejuk.tuddyfuddy.contextservice.dto.response;

import lombok.Builder;

@Builder
public record WeatherLocationResponse(
    String location,
    WeatherResponse todayWeather,
    WeatherResponse yesterdayWeather
) {

}
