package com.heejuk.tuddyfuddy.notificationservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record FcmTokenRequest(

    @Schema(description = "User ID")
    String userId,

    @Schema(description = "FCM Token")
    String fcmToken

) {

}
