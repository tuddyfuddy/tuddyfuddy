package com.heejuk.tuddyfuddy.contextservice.mapper;

import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherResponse;
import com.heejuk.tuddyfuddy.contextservice.entity.Weather;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WeatherMapper {

    WeatherResponse toDto(Weather weather);

    Weather toEntity(WeatherResponse weatherResponse);
}
