package com.heejuk.tuddyfuddy.contextservice.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record WeatherResponse(
    Integer x,
    Integer y,
    LocalDateTime timestamp,
    Double temperature,
    Double humidity,
    String weather,
    LocalDateTime createdAt
) implements Serializable {

}