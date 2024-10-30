package com.heejuk.tuddyfuddy.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

public record CommonResponse<T>(
    @Schema(description = "http status code")
    int statusCode,

    @Schema(description = "response message")
    String message,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    T result
) {
    public static <T> CommonResponse<T> ok(String message) {
        return new CommonResponse<>(HttpStatus.OK.value(), message, null);
    }

    public static <T> CommonResponse<T> ok(String message, T result) {
        return new CommonResponse<>(HttpStatus.OK.value(), message, result);
    }

    public static <T> CommonResponse<T> badRequest(String message) {
        return new CommonResponse<>(HttpStatus.BAD_REQUEST.value(), message, null);
    }

    public static <T> CommonResponse<T> badRequest(String message, T result) {
        return new CommonResponse<>(HttpStatus.BAD_REQUEST.value(), message, result);
    }

    public static <T> CommonResponse<T> created(String message) {
        return new CommonResponse<>(HttpStatus.CREATED.value(), message, null);
    }

    public static <T> CommonResponse<T> unauthorized(String message) {
        return new CommonResponse<>(HttpStatus.UNAUTHORIZED.value(), message, null);
    }

    public static <T> CommonResponse<T> forbidden(String message) {
        return new CommonResponse<>(HttpStatus.FORBIDDEN.value(), message, null);
    }

}
