package com.heejuk.tuddyfuddy.contextservice.dto.kafka;

import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherListResponse;
import lombok.Builder;

@Builder
public record KafkaWeatherDto(
    String userId,
    String location,
    WeatherListResponse weathers
) {

}
