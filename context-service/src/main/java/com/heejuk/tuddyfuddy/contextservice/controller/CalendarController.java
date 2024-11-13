package com.heejuk.tuddyfuddy.contextservice.controller;

import com.heejuk.tuddyfuddy.contextservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.KafkaCalendarDto;
import com.heejuk.tuddyfuddy.contextservice.service.kafka.KafkaProducerService;
import com.heejuk.tuddyfuddy.contextservice.util.HeaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/context/calendar")
public class CalendarController {

    private final KafkaProducerService kafkaProducerService;

    @GetMapping
    public CommonResponse<?> getCalendar(
        @RequestHeader HttpHeaders headers,
        @RequestParam("title") String title
    ) {
        String userId = HeaderUtil.getUserHeaderInfo(headers);

        kafkaProducerService.sendCalendarMessage(KafkaCalendarDto.builder()
                                                                 .userId(userId)
                                                                 .todo(title)
                                                                 .build());
        return CommonResponse.ok("캘린더 정보 GET");
    }
}
