package com.heejuk.tuddyfuddy.notificationservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // FCM 관련 에러
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM token not found"),
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "Invalid FCM token"),
    FCM_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send FCM notification"),
    FCM_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FCM configuration error"),

    // AI 관련 에러
    AI_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "AI information not found"),

    // 기타 에러
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String message;
}