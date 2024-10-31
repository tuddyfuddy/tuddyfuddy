package com.heejuk.tuddyfuddy.authservice.client;

import com.heejuk.tuddyfuddy.authservice.dto.response.KakaoUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakao-api", url = "https://kapi.kakao.com")
public interface KakaoApiClient {

    @GetMapping("/v2/user/me")
    KakaoUserInfo getKakaoUserInfo(@RequestHeader("Authorization") String bearerToken);

}
