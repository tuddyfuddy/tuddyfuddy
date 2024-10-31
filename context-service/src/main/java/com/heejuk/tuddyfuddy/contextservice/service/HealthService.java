package com.heejuk.tuddyfuddy.contextservice.service;

import com.heejuk.tuddyfuddy.contextservice.detector.HealthAnomalyDetector;
import com.heejuk.tuddyfuddy.contextservice.dto.request.HealthRequest;
import com.heejuk.tuddyfuddy.contextservice.dto.response.HealthResponse;
import com.heejuk.tuddyfuddy.contextservice.entity.Health;
import com.heejuk.tuddyfuddy.contextservice.mapper.HealthMapper;
import com.heejuk.tuddyfuddy.contextservice.repository.HealthRepository;
import com.heejuk.tuddyfuddy.contextservice.util.DateUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthService {

    private final HealthRepository healthRepository;
    private final HealthMapper healthMapper;
    private final HealthAnomalyDetector detector;
    private final DateUtil dateUtil;

    public HealthResponse saveHealthData(
        HealthRequest health,
        Long userId
    ) {
        Health entity = healthMapper.toEntity(health);

        entity.setUserId(userId);

        entity.setCreatedAt(dateUtil.getCurrentDateTime());
        entity.setUpdatedAt(dateUtil.getCurrentDateTime());

        Health savedData = healthRepository.save(entity);
        detector.detectAnomalies(savedData);

        return healthMapper.toDto(savedData);
    }

    public List<HealthResponse> getUserHealthData(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    ) {
        return healthMapper.toDtoList(healthRepository.findByUserIdAndTimestampBetween(userId,
                                                                                       start,
                                                                                       end));
    }
}
