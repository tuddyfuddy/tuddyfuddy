package com.heejuk.tuddyfuddy.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.heejuk.tuddyfuddy.userservice.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "사용자 정보 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(

    @Schema(description = "사용자 ID")
    UUID id,

    @Schema(description = "닉네임")
    String nickname,

    @Schema(description = "프로필 이미지 URL")
    String profileImage

) {

    public static UserResponse of(User user) {
        return new UserResponse(
            user.getId(),
            user.getNickname(),
            user.getProfileImage()
        );
    }
}