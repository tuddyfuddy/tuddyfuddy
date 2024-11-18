package com.heejuk.tuddyfuddy.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String aiName;

    @Column(nullable = false)
    private String imageUrl;

    @Builder
    public AiInfo(String aiName, String imageUrl) {
        this.aiName = aiName;
        this.imageUrl = imageUrl;
    }
}
