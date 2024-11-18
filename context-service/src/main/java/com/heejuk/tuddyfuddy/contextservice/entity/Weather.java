package com.heejuk.tuddyfuddy.contextservice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private LocalDate baseDate;
    private LocalTime baseTime;

    // 날씨 데이터
    private Double maxTemperature;
    private Double minTemperature;

    // 날씨
    private String weather;
    // 비고사항(오후에 비가 왔었다, 오전에 눈이 내렸다 이런느낌)
    private String comment;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Weather(
        Integer x,
        Integer y,
        LocalDate baseDate,
        LocalTime baseTime,
        Double maxTemperature,
        Double minTemperature,
        String weather,
        String comment
    ) {
        this.x = x;
        this.y = y;
        this.baseDate = baseDate;
        this.baseTime = baseTime;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.weather = weather;
        this.comment = comment;
    }
}
