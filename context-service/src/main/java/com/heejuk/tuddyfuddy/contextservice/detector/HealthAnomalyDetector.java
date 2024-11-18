package com.heejuk.tuddyfuddy.contextservice.detector;

import com.heejuk.tuddyfuddy.contextservice.detector.strategy.HealthCheckStrategy;
import com.heejuk.tuddyfuddy.contextservice.entity.Health;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthAnomalyDetector {

    private final List<HealthCheckStrategy> strategies;

    /**
     * health data 로 이상 징후 감지
     *
     * @param health
     * @return
     */
    public List<String> detectAnomalies(Health health) {
        return strategies.stream()
                         .filter(strategy -> strategy.isAbnormal(health))
                         .map(strategy -> {
                             log.info("Anomaly detected: {}",
                                      strategy.getMessage(health));
                             return strategy.getMessage(health);
                         })
                         .toList();
    }
}
