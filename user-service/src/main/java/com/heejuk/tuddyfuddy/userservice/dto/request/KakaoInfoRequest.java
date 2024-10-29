package com.heejuk.tuddyfuddy.userservice.dto.request;

public record KakaoInfoRequest(

    Long id,
    String connectedAt,
    Properties properties,
    KakaoAccount kakaoAccount

) {

    public record Properties(
        String nickname,
        String profileImage,
        String thumbnailImage
    ) {

    }

    public record KakaoAccount(
        String email,
        Boolean isEmailValid,
        Boolean isEmailVerified,
        Profile profile,
        Boolean hasAgeRange,
        String ageRange,
        Boolean hasGender,
        String gender
    ) {

        public record Profile(
            String nickname,
            String thumbnailImageUrl,
            String profileImageUrl,
            Boolean isDefaultImage
        ) {

        }
    }
}