package com.heejuk.tuddyfuddy.notificationservice.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.notificationservice.dto.request.ChatMessageRequest;
import com.heejuk.tuddyfuddy.notificationservice.dto.request.FcmTokenRequest;
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

    @KafkaListener(topics = "fcm-token-topic")
    public void consumeFcmToken(String message) throws JsonProcessingException {
        FcmTokenRequest request = objectMapper.readValue(message, FcmTokenRequest.class);
        fcmTokenService.saveToken(request);
    }

    @KafkaListener(topics = "chat-notification-topic")
    public void consumeChatNotification(String message) throws JsonProcessingException {
        ChatMessageRequest request = objectMapper.readValue(message, ChatMessageRequest.class);
        notificationService.sendChatNotification(request);
    }
}
