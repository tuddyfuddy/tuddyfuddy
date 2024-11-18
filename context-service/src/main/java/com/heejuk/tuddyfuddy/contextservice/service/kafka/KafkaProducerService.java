package com.heejuk.tuddyfuddy.contextservice.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.ChatMessageRequest;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.KafkaCalendarDto;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.KafkaWeatherDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private void sendMessage(
        String topic,
        Object message
    ) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, jsonMessage)
                         .whenComplete((result, ex) -> {
                             if (ex == null) {
                                 log.info("Message sent successfully to topic: {}", topic);
                             } else {
                                 log.error("Failed to send message to topic: {}", topic, ex);
                             }
                         });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message", e);
            throw new RuntimeException("Message serialization failed", e);
        }
    }

    public void sendChatMessage(ChatMessageRequest request) {
        String topic = "chat-notification-topic";

        sendMessage(topic, request);
    }

    public void sendWeatherMessage(
        KafkaWeatherDto message
    ) {
        String topic = "chat-weather-topic";

        sendMessage(topic, message);
    }

    public void sendCalendarMessage(
        KafkaCalendarDto message
    ) {
        String topic = "chat-calendar-topic";

        sendMessage(topic, message);
    }
}
