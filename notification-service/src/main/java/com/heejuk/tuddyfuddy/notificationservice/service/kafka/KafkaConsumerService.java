package com.heejuk.tuddyfuddy.notificationservice.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.notificationservice.dto.request.ChatMessageRequest;
import com.heejuk.tuddyfuddy.notificationservice.dto.request.FcmTokenRequest;
import com.heejuk.tuddyfuddy.notificationservice.entity.FcmToken;
import com.heejuk.tuddyfuddy.notificationservice.service.FcmTokenService;
import com.heejuk.tuddyfuddy.notificationservice.service.NotificationService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationService notificationService;
    private final FcmTokenService fcmTokenService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "fcm-token-topic")
    public void consumeFcmToken(String kafkaMessage, Acknowledgment ack) {
        log.info("Received FCM token message: {}", kafkaMessage);
        try {
            Map<Object, Object> map = parseMessage(kafkaMessage);

            FcmTokenRequest request = FcmTokenRequest.builder()
                .userId((String) map.get("userId"))
                .fcmToken((String) map.get("fcmToken"))
                .build();

            fcmTokenService.saveToken(request);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing FCM token message: {}", kafkaMessage, e);
//            throw new CustomException(ErrorCode.KAFKA_ERROR);
        }
    }

    private Map<Object, Object> parseMessage(String kafkaMessage) {
        Map<Object, Object> map;
        ObjectMapper mapper = new ObjectMapper();

        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<HashMap<Object, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return map;
    }
}
