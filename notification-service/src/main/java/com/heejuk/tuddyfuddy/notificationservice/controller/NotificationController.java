package com.heejuk.tuddyfuddy.notificationservice.controller;

import com.heejuk.tuddyfuddy.notificationservice.dto.request.ChatMessageRequest;
import com.heejuk.tuddyfuddy.notificationservice.exception.NotificationException;
import com.heejuk.tuddyfuddy.notificationservice.service.FcmTokenService;
import com.heejuk.tuddyfuddy.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@Tag(name = "알림 관리", description = "알림 TEST API 입니다.")
public class NotificationController {

    private final NotificationService notificationService;
    private final FcmTokenService fcmTokenService;

    @PostMapping("/chat")
    public ResponseEntity<Void> sendChatNotification(@RequestBody ChatMessageRequest request) {
        log.info("채팅 알림 요청 수신 - userId: {}, aiName: {}", request.userId(), request.aiName());

        try {
            notificationService.sendChatNotification(request);
            return ResponseEntity.ok().build();

        } catch (NotificationException e) {
            log.error("알림 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus()).build();
        }
    }

    @GetMapping("/check-cache/{userId}")
    public ResponseEntity<Void> checkCache(@PathVariable String userId) {
        fcmTokenService.checkRedisCache(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-all-cache")
    public ResponseEntity<Void> checkAllCache() {
        fcmTokenService.checkAllCachedTokens();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-cache/{userId}")
    public ResponseEntity<Void> refreshCache(@PathVariable String userId) {
        fcmTokenService.refreshCache(userId);
        return ResponseEntity.ok().build();
    }

}
