package com.heejuk.tuddyfuddy.notificationservice.handler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.heejuk.tuddyfuddy.notificationservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.notificationservice.exception.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<CommonResponse<?>> handleNotificationException(NotificationException e) {
        log.error("NotificationException: {}", e.getMessage(), e);
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(new CommonResponse<>(
                e.getErrorCode().getStatus().value(),
                e.getMessage(),
                null
            ));
    }

    @ExceptionHandler(FirebaseMessagingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResponse<?> handleFirebaseMessagingException(FirebaseMessagingException e) {
        log.error("FirebaseMessagingException: {}", e.getMessage(), e);
        String message = switch (e.getMessagingErrorCode()) {
            case INVALID_ARGUMENT -> "Invalid FCM token";
            case UNREGISTERED -> "FCM token is no longer valid";
            case SENDER_ID_MISMATCH -> "FCM sender ID mismatch";
            default -> "Failed to send FCM message";
        };
        return CommonResponse.internalServerError(message);
    }
}
