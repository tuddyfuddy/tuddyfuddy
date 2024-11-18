package com.heejuk.tuddyfuddy.contextservice.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record HealthResponse(
    Integer heartRate,
    Integer steps,
    Integer sleepMinutes,
    Integer stressLevel,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
