package com.heejuk.tuddyfuddy.authservice.client;

import com.heejuk.tuddyfuddy.authservice.dto.response.KakaoTokenResponse;
import com.heejuk.tuddyfuddy.authservice.dto.response.KakaoUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakao", url = "https://kauth.kakao.com")
public interface KakaoClient {

    @PostMapping("/oauth/token")
    KakaoTokenResponse getAccessToken(
        @RequestParam("grant_type") String grantType,
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("code") String code
    );

    @GetMapping(value = "/v2/user/me")
    KakaoUserResponse getUserInfo(@RequestHeader("Authorization") String bearerToken);

}