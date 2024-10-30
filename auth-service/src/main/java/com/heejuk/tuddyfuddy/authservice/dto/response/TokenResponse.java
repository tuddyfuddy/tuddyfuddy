package com.heejuk.tuddyfuddy.authservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 응답")
public record TokenResponse(

    @Schema(description = "JWT 액세스 토큰")
    String accessToken

) {

}