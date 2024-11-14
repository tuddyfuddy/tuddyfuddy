package com.heejuk.tuddyfuddy.contextservice.dto.kafka;

import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherLocationResponse;
import lombok.Builder;

@Builder
public record KafkaWeatherDto(
    String userId,
    WeatherLocationResponse data
) {

}
