package com.heejuk.tuddyfuddy.contextservice.controller;

import com.heejuk.tuddyfuddy.contextservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.request.HealthRequest;
import com.heejuk.tuddyfuddy.contextservice.dto.response.HealthResponse;
import com.heejuk.tuddyfuddy.contextservice.service.HealthService;
import com.heejuk.tuddyfuddy.contextservice.util.HeaderUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/context/health")
public class HealthController {

    private final HealthService healthService;

    @PostMapping
    public CommonResponse<HealthResponse> createHealthData(
        @RequestHeader HttpHeaders headers,
        @RequestBody HealthRequest health
    ) {
        String userId = HeaderUtil.getUserHeaderInfo(headers);
        log.info("userId = " + userId);

        return CommonResponse.ok("Health data created successfully",
                                 healthService.saveHealthData(
                                     health,
                                     userId));
    }

    @GetMapping("/me")
    public CommonResponse<List<HealthResponse>> getUserHealthData(
        @RequestHeader HttpHeaders headers,
        @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        String userId = HeaderUtil.getUserHeaderInfo(headers);
        log.info("userId = " + userId);
        LocalDateTime now = LocalDateTime.now();

        // 기본 시작시간: 어제 00:00:00
        LocalDateTime defaultStart = now.minusDays(1)
                                        .withHour(0)
                                        .withMinute(0)
                                        .withSecond(0)
                                        .withNano(0);

        // 기본 종료시간: 현재 시간
        LocalDateTime defaultEnd = now;

        LocalDateTime actualStart = start != null ? start : defaultStart;
        LocalDateTime actualEnd = end != null ? end : defaultEnd;

        // 시작시간이 종료시간보다 늦을 경우 예외 처리
        if (actualStart.isAfter(actualEnd)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        return CommonResponse.ok("Health Data Loaded successfully",
                                 healthService.getUserHealthData(userId,
                                                                 start,
                                                                 end));
    }
}
