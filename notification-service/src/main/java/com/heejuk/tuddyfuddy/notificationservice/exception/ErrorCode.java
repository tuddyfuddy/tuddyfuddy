package com.heejuk.tuddyfuddy.notificationservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // FCM 토큰 관련 에러
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM 토큰을 찾을 수 없습니다"),
    FCM_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 FCM 토큰입니다"),
    FCM_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 FCM 토큰입니다"),

    // FCM 전송 관련 에러
    FCM_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 알림 전송에 실패했습니다"),
    FCM_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 설정 오류가 발생했습니다"),
    FCM_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "FCM 할당량이 초과되었습니다"),
    FCM_INVALID_MESSAGE(HttpStatus.BAD_REQUEST, "잘못된 FCM 메시지 형식입니다"),
    FCM_DEVICE_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "등록되지 않은 디바이스입니다"),
    FCM_SENDER_ID_MISMATCH(HttpStatus.UNAUTHORIZED, "FCM Sender ID가 일치하지 않습니다"),

    // AI 관련 에러
    AI_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "AI 정보를 찾을 수 없습니다"),
    AI_NAME_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 AI 이름입니다"),

    // 요청 관련 에러
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 파라미터입니다"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "필수 필드가 누락되었습니다"),

    // 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다");

    private final HttpStatus status;
    private final String message;

    public String getCode() {
        return this.name();
    }
}
