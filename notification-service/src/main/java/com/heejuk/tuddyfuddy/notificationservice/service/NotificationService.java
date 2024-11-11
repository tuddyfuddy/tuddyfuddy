package com.heejuk.tuddyfuddy.notificationservice.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
        log.info("알림 전송 시작 - userId: {}, aiName: {}", request.userId(), request.aiName());

        try {
            // AI 정보 조회
            log.info("AI 정보 조회 시작 - aiName: {}", request.aiName());
            AiInfo aiInfo = aiInfoRepository.findByAiName(request.aiName())
                .orElseThrow(() -> {
                    log.error("AI 정보를 찾을 수 없음 - aiName: {}", request.aiName());
                    return new NotificationException(ErrorCode.AI_INFO_NOT_FOUND);
                });
            log.info("AI 정보 조회 성공 - aiInfo: {}", aiInfo);

            // FCM 토큰 조회
            log.info("FCM 토큰 조회 시작 - userId: {}", request.userId());
            String fcmToken = fcmTokenService.getFcmToken(request.userId())
                .orElseThrow(() -> {
                    log.error("FCM 토큰을 찾을 수 없음 - userId: {}", request.userId());
                    return new NotificationException(ErrorCode.FCM_TOKEN_NOT_FOUND);
                });
            log.info("FCM 토큰 조회 성공 - token: {}", maskToken(fcmToken));

            Map<String, String> data = new HashMap<>();
            data.put("aiName", aiInfo.getAiName());
            data.put("roomId", String.valueOf(request.roomId()));
            data.put("imageUrl", aiInfo.getImageUrl());
            data.put("messageType", "CHAT");
            log.info("알림 데이터 설정 완료 - data: {}", data);

            Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                    .setTitle(aiInfo.getAiName() + "의 메시지")
                    .setBody(request.message())
                    .setImage(aiInfo.getImageUrl())
                    .build())
                .setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                        .setIcon(aiInfo.getImageUrl())
                        .setColor("#7E57C2")
                        .setChannelId("chat_notifications")
                        .setClickAction("CHAT_ACTIVITY")
                        .build())
                    .build())
                .putAllData(data)
                .build();
            log.info("FCM 메시지 생성 완료");

            // FCM으로 메시지 전송
            log.info("FCM 메시지 전송 시작 - userId: {}", request.userId());
            String response = firebaseMessaging.send(message);
            log.info("FCM 메시지 전송 성공 - response: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 에러 발생 - userId: {}, errorCode: {}, message: {}",
                request.userId(), e.getMessagingErrorCode(), e.getMessage());
            handleFcmError(request.userId(), e);
        } catch (Exception e) {
            log.error("알림 전송 실패 - userId: {}, error: {}", request.userId(), e.getMessage(), e);
            throw new NotificationException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleFcmError(String userId, FirebaseMessagingException e) {
        String errorMessage = e.getMessage();
        ErrorCode errorCode;

        log.error("FCM 에러 상세 - userId: {}, errorCode: {}, message: {}",
            userId, e.getMessagingErrorCode(), errorMessage);

        switch (e.getMessagingErrorCode()) {
            case SENDER_ID_MISMATCH -> {
                log.error("FCM sender ID 불일치 - 설정을 확인해주세요.");
                errorCode = ErrorCode.FCM_CONFIGURATION_ERROR;
            }
            case INTERNAL -> {
                log.error("FCM 내부 에러: {}", errorMessage);
                errorCode = ErrorCode.FCM_SEND_ERROR;
            }
            case UNREGISTERED -> {
                log.error("FCM 토큰이 더 이상 유효하지 않음 - userId: {}", userId);
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

    // FCM 토큰 마스킹 처리 (로그에 전체 토큰이 노출되지 않도록)
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return token;
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
