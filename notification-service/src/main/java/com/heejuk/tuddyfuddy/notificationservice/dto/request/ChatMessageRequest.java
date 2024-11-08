package com.heejuk.tuddyfuddy.notificationservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ChatMessageRequest(

    @Schema(description = "User ID")
    String userId,

    @Schema(description = "Room ID")
    Integer roomId,

    @Schema(description = "AI Name")
    String aiName,

    @Schema(description = "Chat message content")
    String message

) {

}
