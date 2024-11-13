package com.heejuk.tuddyfuddy.contextservice.dto.kafka;

import lombok.Builder;

@Builder
public record KafkaCalendarDto(
    String userId,
    String todo
) {

}
