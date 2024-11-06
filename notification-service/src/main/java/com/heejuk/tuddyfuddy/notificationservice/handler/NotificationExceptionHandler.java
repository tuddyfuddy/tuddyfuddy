package com.heejuk.tuddyfuddy.notificationservice.handler;

import com.heejuk.tuddyfuddy.notificationservice.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<?> handleClientException(Exception e) {
        log.error(e.getMessage(), e);
        return CommonResponse.badRequest(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResponse<?> handleServerException(Exception e) {
        log.error(e.getMessage(), e);
        return CommonResponse.internalServerError(e.getMessage());
    }
}
