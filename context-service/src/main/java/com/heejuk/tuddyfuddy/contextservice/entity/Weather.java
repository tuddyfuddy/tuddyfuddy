package com.heejuk.tuddyfuddy.contextservice.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "weatherData")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Weather {

    @Id
    private String id;

    private Integer x;
    private Integer y;
    private LocalDateTime timestamp;

    // 날씨 데이터
    private Double temperature;
    private Double humidity; // 습도
    private String weather; // 날씨

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Weather(
        Integer x,
        Integer y,
        LocalDateTime timestamp,
        Double temperature,
        Double humidity,
        String weather,
        LocalDateTime createdAt
    ) {
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.weather = weather;
        this.createdAt = createdAt;
    }
}
