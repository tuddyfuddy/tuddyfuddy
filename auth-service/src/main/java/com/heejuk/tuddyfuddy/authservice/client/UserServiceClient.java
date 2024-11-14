package com.heejuk.tuddyfuddy.authservice.client;

import com.heejuk.tuddyfuddy.authservice.circuit.UserServiceClientFallback;
import com.heejuk.tuddyfuddy.authservice.config.FeignConfig;
import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.authservice.dto.response.KakaoUserInfo;
import com.heejuk.tuddyfuddy.authservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "user-service",
    fallback = UserServiceClientFallback.class,
    configuration = FeignConfig.class
)
public interface UserServiceClient {

    @PostMapping("/users/kakao")
    CommonResponse<UserResponse> loginKakaoUser(@RequestBody KakaoUserInfo kakaoUserInfo);

}
