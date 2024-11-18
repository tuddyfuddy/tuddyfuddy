package com.heejuk.tuddyfuddy.authservice.dto.response;

import lombok.Builder;

@Builder
public record FcmTokenMessageResponse(

    String userId,
    String fcmToken

) {

    public static FcmTokenMessageResponse of(String userId, String fcmToken) {
        return FcmTokenMessageResponse.builder()
            .userId(userId)
            .fcmToken(fcmToken)
            .build();
    }
}
