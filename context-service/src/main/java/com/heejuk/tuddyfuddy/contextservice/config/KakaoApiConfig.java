package com.heejuk.tuddyfuddy.contextservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoApiConfig {

    @Value("${kakao.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor kakaoApiKeyInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "KakaoAK " + apiKey);
        };
    }
}
