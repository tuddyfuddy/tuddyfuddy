package com.heejuk.tuddyfuddy.authservice.client;

import com.heejuk.tuddyfuddy.authservice.dto.response.KakaoUserResponse;
import com.heejuk.tuddyfuddy.authservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping("/users/login")
    UserResponse processKakaoUser(@RequestBody KakaoUserResponse kakaoUserInfo);

    @GetMapping("/users/{userId}")
    UserResponse getUser(@PathVariable String userId);

}
