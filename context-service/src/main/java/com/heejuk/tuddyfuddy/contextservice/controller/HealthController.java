package com.heejuk.tuddyfuddy.contextservice.controller;

import com.heejuk.tuddyfuddy.contextservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.contextservice.entity.Health;
import com.heejuk.tuddyfuddy.contextservice.service.HealthService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/context/health")
public class HealthController {

    private final HealthService healthService;

    @PostMapping
    public CommonResponse<Health> createHealthData(@RequestBody Health health) {
        return CommonResponse.ok("Health data created successfully",
                                 healthService.saveHealthData(
                                     health));
    }

    @GetMapping("/{userId}")
    public CommonResponse<List<Health>> getUserHealthData(
        @PathVariable Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return CommonResponse.ok("Health Data Loaded successfully",
                                 healthService.getUserHealthData(userId,
                                                                 start,
                                                                 end));
    }
}
