package com.heejuk.tuddyfuddy.authservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 응답")
public record UserResponse(

    @Schema(description = "사용자 ID")
    Long id,

    @Schema(description = "이메일")
    String email,

    @Schema(description = "닉네임")
    String nickname,

    @Schema(description = "프로필 이미지 URL")
    String profileImage,

    @Schema(description = "생년월일", example = "YYYY-MM-DD")
    String birthDate

) {

}
