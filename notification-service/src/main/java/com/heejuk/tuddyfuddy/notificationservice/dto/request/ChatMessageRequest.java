package com.heejuk.tuddyfuddy.notificationservice.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    String message,

    @Schema(description = "Chat message type")
    String messageType

) {

    @JsonCreator
    public ChatMessageRequest(
        @JsonProperty("userId") String userId,
        @JsonProperty("roomId") Integer roomId,
        @JsonProperty("aiName") String aiName,
        @JsonProperty("message") String message,
        @JsonProperty("messageType") String messageType
    ) {
        this.userId = userId;
        this.roomId = roomId;
        this.aiName = aiName;
        this.message = message;
        this.messageType = (messageType == null || messageType.isEmpty()) ? "CHAT" : messageType;
    }
}
