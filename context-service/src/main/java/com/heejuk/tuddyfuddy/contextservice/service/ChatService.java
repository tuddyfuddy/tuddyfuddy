package com.heejuk.tuddyfuddy.contextservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.KafkaCalendarDto;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.KafkaWeatherDto;
import com.heejuk.tuddyfuddy.contextservice.util.ChatPromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public String sendWeatherChat(KafkaWeatherDto weatherData) {
        try {
            // DTO를 JSON 문자열로 변환
            String weatherJson = objectMapper.writeValueAsString(weatherData);

            return sendChat(weatherJson, ChatPromptTemplate.WEATHER_RESPONSE_TEMPLATE);
        } catch (Exception e) {
            log.error("Error processing weather data", e);
            throw new RuntimeException("날씨 데이터 처리 중 오류가 발생했습니다", e);
        }
    }

    public String sendCalendarChat(KafkaCalendarDto calendarData) {
        try {
            // DTO를 JSON 문자열로 변환
            String calendarJson = objectMapper.writeValueAsString(calendarData);

            return sendChat(calendarJson, ChatPromptTemplate.CALENDAR_RESPONSE_TEMPLATE);
        } catch (Exception e) {
            log.error("Error processing calendar data", e);
            throw new RuntimeException("캘린더 데이터 처리 중 오류가 발생했습니다", e);
        }
    }

    private String sendChat(
        String message,
        String promptTemplate
    ) {
        try {
            String processedPrompt = promptTemplate
                .replace("{weather_data}", message)
                .replace("{calendar_data}", message);

            // ChatClient로 프롬프트 전송 및 응답 받기
            String response = chatClient.prompt()
                                        .system(processedPrompt)  // 시스템 프롬프트로 설정
                                        .call()
                                        .content();

            log.info("AI Response generated successfully");
            return response;

        } catch (Exception e) {
            log.error("Error generating AI response", e);
            throw new RuntimeException("AI 응답 생성 중 오류가 발생했습니다", e);
        }
    }
}
