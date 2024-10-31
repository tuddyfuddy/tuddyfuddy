package com.heejuk.tuddyfuddy.contextservice.detector.strategy;

import com.heejuk.tuddyfuddy.contextservice.detector.config.HealthThresholdConfig;
import com.heejuk.tuddyfuddy.contextservice.entity.Health;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StressLevelStrategy implements HealthCheckStrategy {

    private final HealthThresholdConfig thresholdConfig;

    @Override
    public boolean isAbnormal(Health data) {
        if (data.getStressLevel() == null) {
            return false;
        }

        int stressLevel = data.getStressLevel();
        return stressLevel > thresholdConfig.getNormalStressLevel();
    }

    @Override
    public String getMessage(Health data) {
        return getStressMessage(data.getStressLevel());
    }

    private String getStressMessage(Integer stressLevel) {
        if (stressLevel > thresholdConfig.getSevereStressLevel()) {
            return "[위험] 스트레스 지수가 매우 높습니다. 전문의 상담이 필요합니다.";
        } else if (stressLevel > thresholdConfig.getModerateStressLevel()) {
            return "[경고] 스트레스 지수가 높습니다. 휴식이 필요합니다.";
        } else if (stressLevel > thresholdConfig.getMildStressLevel()) {
            return "[주의] 스트레스 초기 단계입니다. 관리가 필요합니다.";
        } else if (stressLevel > thresholdConfig.getNormalStressLevel()) {
            return "[알림] 스트레스 지수가 약간 상승했습니다.";
        }
        return null;
    }
}
