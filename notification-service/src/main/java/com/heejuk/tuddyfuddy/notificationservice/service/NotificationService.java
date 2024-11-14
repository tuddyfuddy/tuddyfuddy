package com.heejuk.tuddyfuddy.notificationservice.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.heejuk.tuddyfuddy.notificationservice.dto.request.ChatMessageRequest;
import com.heejuk.tuddyfuddy.notificationservice.entity.AiInfo;
import com.heejuk.tuddyfuddy.notificationservice.exception.ErrorCode;
import com.heejuk.tuddyfuddy.notificationservice.exception.NotificationException;
import com.heejuk.tuddyfuddy.notificationservice.repository.AiInfoRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FcmTokenService fcmTokenService;
    private final AiInfoRepository aiInfoRepository;
    private final FirebaseMessaging firebaseMessaging;

    public void sendChatNotification(ChatMessageRequest request) {
        log.info("알림 전송 시작 - userId: {}, aiName: {}, message: {}",
            request.userId(), request.aiName(), request.message());

        try {
            // AI 정보 조회
            log.debug("AI 정보 조회 시작 - aiName: {}", request.aiName());
            AiInfo aiInfo = aiInfoRepository.findByAiName(request.aiName())
                .orElseThrow(() -> {
                    log.error("AI 정보를 찾을 수 없음 - aiName: {}", request.aiName());
                    return new NotificationException(ErrorCode.AI_INFO_NOT_FOUND);
                });
            log.debug("AI 정보 조회 성공 - aiName: {}, imageUrl: {}",
                aiInfo.getAiName(), aiInfo.getImageUrl());

            // FCM 토큰 조회
            log.debug("FCM 토큰 조회 시작 - userId: {}", request.userId());
            String fcmToken = fcmTokenService.getFcmToken(request.userId())
                .orElseThrow(() -> {
                    log.error("FCM 토큰을 찾을 수 없음 - userId: {}", request.userId());
                    return new NotificationException(ErrorCode.FCM_TOKEN_NOT_FOUND);
                });
            log.debug("FCM 토큰 조회 성공 - token: {}", maskToken(fcmToken));

            // 토큰 유효성 사전 검증
            validateFcmToken(fcmToken, request.userId());

            // 알림 데이터 설정
            Map<String, String> data = new HashMap<>();
            data.put("aiName", aiInfo.getAiName());
            data.put("roomId", String.valueOf(request.roomId()));
            data.put("icon", getIconNameFromUrl(aiInfo.getImageUrl()));
            data.put("messageType", "CHAT");
            data.put("message", request.message());
            data.put("color", "#7E57C2");
            log.debug("알림 데이터 설정 완료 - data: {}", data);

            Message message = Message.builder()
                .setToken(fcmToken)
                .putAllData(data)  // data만 전송
                .build();
            log.debug("FCM 메시지 생성 완료");

            // FCM으로 메시지 전송
            log.info("FCM 메시지 전송 시작 - userId: {}, title: {}",
                request.userId(), aiInfo.getAiName() + "의 메시지");

            String response = firebaseMessaging.send(message);
            log.info("FCM 메시지 전송 성공 - response: {}, userId: {}", response, request.userId());

        } catch (FirebaseMessagingException e) {
            log.error("FCM 에러 발생 - userId: {}, errorCode: {}, message: {}, stackTrace: {}",
                request.userId(),
                e.getMessagingErrorCode(),
                e.getMessage(),
                e.getStackTrace());
            handleFcmError(request.userId(), e);
        } catch (Exception e) {
            log.error("알림 전송 실패 - userId: {}, error: {}", request.userId(), e.getMessage(), e);
            throw new NotificationException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String getIconNameFromUrl(String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);  // 파일명 추출
        String iconName = fileName.substring(0, fileName.lastIndexOf('.'));   // 확장자 제거
        return "@drawable/" + iconName;  // drawable 리소스 형식으로 변환
    }

    private void validateFcmToken(String token, String userId) {
        try {
            Message testMessage = Message.builder()
                .setToken(token)
                .build();
            firebaseMessaging.send(testMessage);
            log.debug("FCM 토큰 유효성 검증 성공 - userId: {}", userId);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 토큰 유효성 검증 실패 - userId: {}, error: {}", userId, e.getMessage());
            handleFcmError(userId, e);
        }
    }

    private void handleFcmError(String userId, FirebaseMessagingException e) {
        String errorMessage = e.getMessage();
        ErrorCode errorCode;

        log.error("FCM 에러 상세 - userId: {}, errorCode: {}, message: {}, stackTrace: {}",
            userId, e.getMessagingErrorCode(), errorMessage, e.getStackTrace());

        switch (e.getMessagingErrorCode()) {
            case SENDER_ID_MISMATCH -> {
                log.error("FCM sender ID 불일치 - Firebase 프로젝트 설정을 확인해주세요.");
                errorCode = ErrorCode.FCM_CONFIGURATION_ERROR;
            }
            case INTERNAL -> {
                log.error("FCM 내부 에러 - Firebase 서버 상태를 확인해주세요: {}", errorMessage);
                errorCode = ErrorCode.FCM_SEND_ERROR;
            }
            case UNREGISTERED -> {
                log.error("FCM 토큰 만료 - userId: {}. 토큰 갱신이 필요합니다.", userId);
                try {
//                    fcmTokenService.removeFcmToken(userId);
                    log.info("만료된 FCM 토큰 삭제 완료 - userId: {}", userId);
                } catch (Exception ex) {
                    log.error("만료된 FCM 토큰 삭제 실패 - userId: {}", userId, ex);
                }
                errorCode = ErrorCode.FCM_TOKEN_INVALID;
            }
            case INVALID_ARGUMENT -> {
                log.error("잘못된 FCM 메시지 형식 - message: {}", errorMessage);
                errorCode = ErrorCode.FCM_INVALID_MESSAGE;
            }
            default -> {
                log.error("알 수 없는 FCM 에러 - errorCode: {}, message: {}",
                    e.getMessagingErrorCode(), errorMessage);
                errorCode = ErrorCode.FCM_SEND_ERROR;
            }
        }

        throw new NotificationException(errorCode, errorMessage);
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "INVALID_TOKEN_FORMAT";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}