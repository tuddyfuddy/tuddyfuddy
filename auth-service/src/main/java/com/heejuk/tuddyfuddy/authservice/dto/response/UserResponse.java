package com.heejuk.tuddyfuddy.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사용자 정보 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(

    @Schema(description = "사용자 ID")
    String userId,

    @Schema(description = "닉네임")
    String nickname,

    @Schema(description = "프로필 이미지 URL")
    String profileImage

) {

}