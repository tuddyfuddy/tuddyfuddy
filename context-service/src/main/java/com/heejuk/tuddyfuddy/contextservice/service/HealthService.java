package com.heejuk.tuddyfuddy.contextservice.service;

import com.heejuk.tuddyfuddy.contextservice.entity.*;
import com.heejuk.tuddyfuddy.contextservice.repository.*;
import java.time.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthService {

    private final HealthRepository healthRepository;

    public Health saveHealthData(Health health) {
        health.setCreatedAt(LocalDateTime.now());
        health.setUpdatedAt(LocalDateTime.now());

        Health savedData = healthRepository.save(health);
        if (isAbnormalHealthData(savedData)) {
            log.info("Abnormal health data detected");
        }
        return savedData;
    }

    /**
     * 이상 징후 감지
     *
     * @param data
     * @return
     */
    private boolean isAbnormalHealthData(Health data) {
        if (data.getHeartRate() != null && data.getHeartRate() > 200) {
            log.info("[위험] 심박수가 200 이상입니다.");
            return true;
        }
        return false;
    }

    public List<Health> getUserHealthData(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    ) {
        return healthRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }
}
