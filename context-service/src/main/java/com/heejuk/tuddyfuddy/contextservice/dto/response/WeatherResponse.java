package com.heejuk.tuddyfuddy.contextservice.dto.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record WeatherResponse(
    Integer x,
    Integer y,
    LocalDate timestamp,
    Double minTemperature,
    Double maxTemperature,
    String weather,
    String note,
    LocalDateTime createdAt
) implements Serializable {

}