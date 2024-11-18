package com.heejuk.tuddyfuddy.authservice.handler;

import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.authservice.exception.AuthenticationException;
import com.heejuk.tuddyfuddy.authservice.exception.CircuitBreakerException;
import feign.FeignException.FeignClientException;
import feign.FeignException.FeignServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResponse<?> handleAuthenticationException(AuthenticationException e) {
        log.error(e.getMessage(), e);
        return CommonResponse.unauthorized(e.getMessage());
    }

    @ExceptionHandler(FeignClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<?> handleFeignClientException(FeignClientException e) {
        log.error(e.getMessage(), e);
        return CommonResponse.badRequest(e.getMessage());
    }

    @ExceptionHandler(FeignServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResponse<?> handleFeignServerException(FeignServerException e) {
        log.error(e.getMessage(), e);
        return CommonResponse.internalServerError(e.getMessage());
    }

    @ExceptionHandler(CircuitBreakerException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public CommonResponse<?> handleCircuitBreakerException(CircuitBreakerException e) {
        log.error("Circuit Breaker 작동: {}", e.getMessage(), e);
        return CommonResponse.error(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요."
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResponse<?> handleServerException(Exception e) {
        log.error(e.getMessage(), e);
        return CommonResponse.internalServerError(e.getMessage());
    }
}
