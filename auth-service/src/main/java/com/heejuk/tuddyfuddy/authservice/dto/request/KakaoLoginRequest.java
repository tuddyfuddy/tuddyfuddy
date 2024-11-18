package com.heejuk.tuddyfuddy.authservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 로그인 요청")
public record KakaoLoginRequest(

    @Schema(description = "카카오 액세스 토큰")
    String accessToken,

    @Schema(description = "FCM 토큰")
    String fcmToken

) {

}