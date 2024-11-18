package com.heejuk.tuddyfuddy.imageservice.handler;

import com.heejuk.tuddyfuddy.imageservice.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ImageExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<?> handleRuntimeException(RuntimeException e) {
        return CommonResponse.badRequest(e.getMessage());
    }
}
