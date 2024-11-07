package com.heejuk.tuddyfuddy.contextservice.dto.response;

import lombok.Builder;

@Builder
public record WeatherListResponse(
    WeatherResponse todayWeather,
    WeatherResponse yesterdayWeather
) {

}
