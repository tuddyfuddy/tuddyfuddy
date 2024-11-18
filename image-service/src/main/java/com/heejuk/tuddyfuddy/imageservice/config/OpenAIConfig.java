package com.heejuk.tuddyfuddy.imageservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Bean
    public ChatClient chatClient() {
        ChatModel chatModel = new OpenAiChatModel(new OpenAiApi(apiKey));
        return ChatClient
            .builder(chatModel)
            .build();
    }

    @Bean
    public OpenAiImageModel imageModel() {
        return new OpenAiImageModel(new OpenAiImageApi(apiKey));
    }
}
