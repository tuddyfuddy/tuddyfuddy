package com.heejuk.tuddyfuddy.contextservice.detector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "health.threshold")
@Component
@Getter
@Setter
public class HealthThresholdConfig {

    private int maxHeartRate;
    private int minHeartRate;

    // 스트레스 지수 임계값 (갤럭시 워치 기준)
    private int normalStressLevel;    // 정상
    private int mildStressLevel;      // 초기 스트레스
    private int moderateStressLevel;  // 주의 필요
    private int severeStressLevel;    // 위험, 전문의 상담 필요

    private int minSleepMinutes;
    private int maxSleepMinutes;
}