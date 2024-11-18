package com.heejuk.tuddyfuddy.contextservice.dto.request;

import lombok.Builder;

@Builder
public record HealthRequest(
    Integer heartRate,
    Integer steps,
    Integer sleepMinutes,
    Integer stressLevel
) {

}
