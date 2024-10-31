package com.heejuk.tuddyfuddy.contextservice.detector.strategy;

import com.heejuk.tuddyfuddy.contextservice.entity.Health;

public interface HealthCheckStrategy {

    boolean isAbnormal(Health data);

    String getMessage(Health data);
}
