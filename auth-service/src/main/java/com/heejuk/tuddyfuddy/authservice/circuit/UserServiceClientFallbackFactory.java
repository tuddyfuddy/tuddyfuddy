package com.heejuk.tuddyfuddy.authservice.circuit;

import com.heejuk.tuddyfuddy.authservice.client.UserServiceClient;
import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.authservice.dto.response.KakaoUserInfo;
import com.heejuk.tuddyfuddy.authservice.dto.response.UserResponse;
import com.heejuk.tuddyfuddy.authservice.exception.FeignServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public CommonResponse<UserResponse> loginKakaoUser(KakaoUserInfo kakaoUserInfo) {
                log.error("User Service 호출 실패 - Circuit Breaker 발동. kakaoId: {}, error: {}",
                    kakaoUserInfo.id(), cause.getMessage());

                throw new FeignServerException("User Service 연결 실패", 503);
            }
        };
    }
}