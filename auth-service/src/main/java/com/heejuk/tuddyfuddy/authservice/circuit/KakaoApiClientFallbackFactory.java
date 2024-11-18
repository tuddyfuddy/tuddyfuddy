package com.heejuk.tuddyfuddy.authservice.circuit;

import com.heejuk.tuddyfuddy.authservice.client.KakaoApiClient;
import com.heejuk.tuddyfuddy.authservice.dto.response.KakaoUserInfo;
import com.heejuk.tuddyfuddy.authservice.exception.FeignServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KakaoApiClientFallbackFactory implements FallbackFactory<KakaoApiClient> {

    @Override
    public KakaoApiClient create(Throwable cause) {
        return new KakaoApiClient() {
            @Override
            public KakaoUserInfo getKakaoUserInfo(String bearerToken) {
                log.error("카카오 API 호출 실패 - Circuit Breaker 발동. token: {}, error: {}",
                    maskToken(bearerToken), cause.getMessage());

                throw new FeignServerException("카카오 API 서버 연결 실패", 503);
            }

            // 토큰 마스킹 유틸리티 메소드
            private String maskToken(String token) {
                if (token == null) {
                    return null;
                }
                if (token.length() <= 8) {
                    return "***";
                }
                return token.substring(0, 4) + "..." +
                    token.substring(token.length() - 4);
            }
        };
    }
}