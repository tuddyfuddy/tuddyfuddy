package com.heejuk.tuddyfuddy.userservice.dto;

public record KakaoUserInfo(

    Long id,
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
        Profile profile
    ) {

        public record Profile(
            String nickname,
            String thumbnailImageUrl,
            String profileImageUrl
        ) {

        }
    }
}