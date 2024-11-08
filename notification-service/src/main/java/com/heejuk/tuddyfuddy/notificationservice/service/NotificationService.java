package com.heejuk.tuddyfuddy.notificationservice.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
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
        try {
            // AI 정보 조회
            AiInfo aiInfo = aiInfoRepository.findByAiName(request.aiName())
                .orElseThrow(() -> new NotificationException(ErrorCode.AI_INFO_NOT_FOUND));

            // FCM 토큰 조회
            String fcmToken = fcmTokenService.getFcmToken(request.userId())
                .orElseThrow(() -> new NotificationException(ErrorCode.FCM_TOKEN_NOT_FOUND));

            Map<String, String> data = new HashMap<>();
            data.put("aiName", aiInfo.getAiName());
            data.put("roomId", String.valueOf(request.roomId()));
            data.put("imageUrl", aiInfo.getImageUrl());
            data.put("messageType", "CHAT");

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
                .setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                        .setCategory("chat_message")
                        .setSound("default")
                        .build())
                    .build())
                .putAllData(data)
                .build();

            // 5. FCM으로 메시지 전송
            String response = firebaseMessaging.send(message);
            log.info("Successfully sent FCM message for user: {}, response: {}",
                request.userId(), response);

        } catch (FirebaseMessagingException e) {
            handleFcmError(request.userId(), e);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            throw new NotificationException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleFcmError(String userId, FirebaseMessagingException e) {
        String errorMessage = e.getMessage();
        ErrorCode errorCode;

        switch (e.getMessagingErrorCode()) {
            case SENDER_ID_MISMATCH -> {
                log.error("FCM sender ID mismatch. Please check configuration.");
                errorCode = ErrorCode.FCM_CONFIGURATION_ERROR;
            }
            case INTERNAL -> {
                log.error("FCM internal error: {}", errorMessage);
                errorCode = ErrorCode.FCM_SEND_ERROR;
            }
            default -> {
                log.error("Unknown FCM error: {}", errorMessage);
                errorCode = ErrorCode.FCM_SEND_ERROR;
            }
        }

        throw new NotificationException(errorCode, errorMessage);
    }
}
