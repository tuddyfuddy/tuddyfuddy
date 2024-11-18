package com.heejuk.tuddyfuddy.contextservice.detector.strategy;

import com.heejuk.tuddyfuddy.contextservice.detector.config.HealthThresholdConfig;
import com.heejuk.tuddyfuddy.contextservice.entity.Health;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeartRateStrategy implements HealthCheckStrategy {

    private final HealthThresholdConfig thresholdConfig;

    @Override
    public boolean isAbnormal(Health data) {
        return data.getHeartRate() != null &&
            (data.getHeartRate() > thresholdConfig.getMaxHeartRate() ||
                data.getHeartRate() < thresholdConfig.getMinHeartRate());
    }

    @Override
    public String getMessage(Health data) {
        return String.format("[위험] 심박수가 정상 범위(%d~%d)를 벗어났습니다.",
                             thresholdConfig.getMinHeartRate(),
                             thresholdConfig.getMaxHeartRate());
    }
}
