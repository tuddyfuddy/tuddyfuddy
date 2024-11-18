package com.heejuk.tuddyfuddy.notificationservice.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.notificationservice.dto.request.ChatMessageRequest;
import com.heejuk.tuddyfuddy.notificationservice.dto.request.FcmTokenRequest;
import com.heejuk.tuddyfuddy.notificationservice.exception.ErrorCode;
import com.heejuk.tuddyfuddy.notificationservice.exception.NotificationException;
import com.heejuk.tuddyfuddy.notificationservice.service.FcmTokenService;
import com.heejuk.tuddyfuddy.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationService notificationService;
    private final FcmTokenService fcmTokenService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "fcm-token-topic", groupId = "${spring.message.kafka.group-id}")
    public void consumeFcmToken(String message) throws JsonProcessingException {
        FcmTokenRequest request = objectMapper.readValue(message, FcmTokenRequest.class);
        fcmTokenService.saveToken(request);
    }

    @KafkaListener(topics = "chat-notification-topic", groupId = "${spring.message.kafka.group-id}")
    public void consumeChatNotification(String message) throws JsonProcessingException {
        try {
            log.info("채팅 알림 메시지 수신: {}", message);

            ChatMessageRequest request = objectMapper.readValue(message, ChatMessageRequest.class);
            log.debug("메시지 변환 완료: {}", request);

            notificationService.sendChatNotification(request);

        } catch (JsonProcessingException e) {
            log.error("메시지 파싱 실패: {}", message, e);
            throw e;
        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", message, e);
            throw new NotificationException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
