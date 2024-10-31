package com.heejuk.tuddyfuddy.contextservice.detector.strategy;

import com.heejuk.tuddyfuddy.contextservice.detector.config.HealthThresholdConfig;
import com.heejuk.tuddyfuddy.contextservice.entity.Health;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SleepDurationStrategy implements HealthCheckStrategy {

    private final HealthThresholdConfig thresholdConfig;

    @Override
    public boolean isAbnormal(Health data) {
        return data.getSleepMinutes() != null &&
            (
                data.getSleepMinutes() > thresholdConfig.getMaxSleepMinutes() ||
                    data.getSleepMinutes() < thresholdConfig.getMinSleepMinutes()
            );
    }

    @Override
    public String getMessage(Health data) {
        return String.format("[주의] 수면 시간이 정상 범위(%d~%d분)를 벗어났습니다.",
                             thresholdConfig.getMinSleepMinutes(),
                             thresholdConfig.getMaxSleepMinutes());
    }
}
