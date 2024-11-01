package com.heejuk.tuddyfuddy.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KakaoUserInfo(

    Long id,

    @JsonProperty("properties")
    Properties properties,

    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount

) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Properties(

        String nickname,

        @JsonProperty("profile_image")
        String profileImage,

        @JsonProperty("thumbnail_image")
        String thumbnailImage
    ) {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record KakaoAccount(

        Profile profile

    ) {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Profile(

            String nickname,

            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl,

            @JsonProperty("profile_image_url")
            String profileImageUrl
        ) {

        }
    }
}