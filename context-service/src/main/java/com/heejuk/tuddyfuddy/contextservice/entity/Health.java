package com.heejuk.tuddyfuddy.contextservice.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "healthData")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Health {

    @Id
    private String id;
    @Setter
    private Long userId;
    private LocalDateTime timestamp;

    // 건강 데이터
    private Integer heartRate;
    private Integer steps;
    private Integer sleepMinutes;
    private Integer stressLevel;

    @Setter
    private LocalDateTime createdAt;
    @Setter
    private LocalDateTime updatedAt;

    @Builder
    public Health(
        Long userId,
        LocalDateTime timestamp,
        Integer heartRate,
        Integer steps,
        Integer sleepMinutes,
        Integer stressLevel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.steps = steps;
        this.sleepMinutes = sleepMinutes;
        this.stressLevel = stressLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
