package com.heejuk.tuddyfuddy.contextservice.controller;

import com.heejuk.tuddyfuddy.contextservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.ChatMessageRequest;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.KafkaCalendarDto;
import com.heejuk.tuddyfuddy.contextservice.service.ChatService;
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
    private final ChatService chatService;

    @GetMapping
    public CommonResponse<String> getCalendar(
        @RequestHeader HttpHeaders headers,
        @RequestParam("title") String title
    ) {
        String userId = HeaderUtil.getUserHeaderInfo(headers);

        KafkaCalendarDto calendarDto = KafkaCalendarDto.builder()
                                                       .userId(userId)
                                                       .todo(title)
                                                       .build();

        String response = chatService.sendCalendarChat(calendarDto);
        kafkaProducerService.sendChatMessage(ChatMessageRequest.builder()
                                                               .userId(userId)
                                                               .roomId(2)
                                                               .aiName("Fuddy")
                                                               .message(response)
                                                               .build());
        return CommonResponse.ok("캘린더 정보 GET", response);
    }
}
