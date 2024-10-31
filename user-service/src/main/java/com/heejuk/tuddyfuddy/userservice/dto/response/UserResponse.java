package com.heejuk.tuddyfuddy.userservice.dto.response;

import com.heejuk.tuddyfuddy.userservice.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Builder;

@Builder
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

    public static UserResponse of(User data) {

        LocalDate birthDate = data.getBirthDate();
        String formattedBirthDate = null;

        if (birthDate != null) {
            formattedBirthDate = birthDate.format(DateTimeFormatter.ISO_DATE); // YYYY-MM-DD
        }

        return UserResponse.builder()
            .id(data.getId())
            .email(data.getEmail())
            .nickname(data.getNickname())
            .profileImage(data.getProfileImage())
            .birthDate(formattedBirthDate)
            .build();
    }
}