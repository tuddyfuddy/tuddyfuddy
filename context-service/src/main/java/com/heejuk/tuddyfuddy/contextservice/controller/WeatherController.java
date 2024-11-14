package com.heejuk.tuddyfuddy.contextservice.controller;

import com.heejuk.tuddyfuddy.contextservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.ChatMessageRequest;
import com.heejuk.tuddyfuddy.contextservice.dto.kafka.KafkaWeatherDto;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherListResponse;
import com.heejuk.tuddyfuddy.contextservice.dto.response.WeatherLocationResponse;
import com.heejuk.tuddyfuddy.contextservice.service.ChatService;
import com.heejuk.tuddyfuddy.contextservice.service.LocationService;
import com.heejuk.tuddyfuddy.contextservice.service.WeatherService;
import com.heejuk.tuddyfuddy.contextservice.service.kafka.KafkaProducerService;
import com.heejuk.tuddyfuddy.contextservice.util.HeaderUtil;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/context/weather")
public class WeatherController {

    private final KafkaProducerService kafkaProducerService;
    private final WeatherService weatherService;
    private final LocationService locationService;
    private final ChatService chatService;

    @PostMapping
    public CommonResponse<String> getWeatherContextData(
        @RequestHeader HttpHeaders headers,
        @RequestParam("latitude") String latitude,
        @RequestParam("longitude") String longitude
    ) {
        String userId = HeaderUtil.getUserHeaderInfo(headers);

        WeatherListResponse weathersByLocation = weatherService.getWeathersByLocation(latitude,
                                                                                      longitude);
        String location = locationService.getLocation(longitude, latitude);

        WeatherLocationResponse res = WeatherLocationResponse.builder()
                                                             .todayWeather(
                                                                 weathersByLocation.todayWeather())
                                                             .yesterdayWeather(
                                                                 weathersByLocation.yesterdayWeather())
                                                             .location(location)
                                                             .build();
        KafkaWeatherDto weatherDto = KafkaWeatherDto.builder()
                                                    .userId(userId)
                                                    .data(res)
                                                    .build();
        String response = chatService.sendWeatherChat(weatherDto);

        kafkaProducerService.sendChatMessage(ChatMessageRequest.builder()
                                                               .userId(userId)
                                                               .roomId(2)
                                                               .aiName("Fuddy")
                                                               .message(response)
                                                               .build());

        return CommonResponse.ok("Weather data fetched successfully",
                                 response
        );
    }

    @PostMapping("/async")
    @Hidden
    public void asdf1() {
        weatherService.dailyFetchAll();
    }

    @PostMapping("/sync")
    @Hidden
    public void asdf2() {
        weatherService.dailyFetchAllSequential();
    }
}
