package com.heejuk.tuddyfuddy.contextservice.handler;

import com.heejuk.tuddyfuddy.contextservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.contextservice.exception.KakaoApiRequestException;
import com.heejuk.tuddyfuddy.contextservice.exception.WeatherApiRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WeatherExceptionHandler {

    @ExceptionHandler(WeatherApiRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<?> handleWeatherApiRequestException(WeatherApiRequestException e) {
        log.error(e.getMessage(), e);
        return CommonResponse.badRequest(e.getMessage());
    }

    @ExceptionHandler(KakaoApiRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<?> handleKakaoApiRequestException(KakaoApiRequestException e) {
        log.error(e.getMessage(), e);
        return CommonResponse.badRequest(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<?> handleRuntimeException(RuntimeException e) {
        log.error(e.getMessage(), e);
        return CommonResponse.badRequest(e.getMessage());
    }
}
