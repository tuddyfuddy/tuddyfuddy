package com.heejuk.tuddyfuddy.contextservice.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;

//    @KafkaListener(topics = "chat-weather-topic")
//    public void consumeChatWeather(
//        String userId,
//        String message
//    ) throws JsonProcessingException {
//
//    }
}
