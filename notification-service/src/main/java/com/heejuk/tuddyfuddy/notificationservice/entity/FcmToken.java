package com.heejuk.tuddyfuddy.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String fcmToken;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Builder
    public FcmToken(UUID userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}