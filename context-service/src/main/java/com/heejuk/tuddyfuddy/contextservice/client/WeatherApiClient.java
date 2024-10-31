package com.heejuk.tuddyfuddy.contextservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.heejuk.tuddyfuddy.contextservice.util.FormatUtil;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherApiClient {

    private final String datePattern = "yyyyMMdd";
    private final String timePattern = "HHmm";


    private final WebClient webClient;
    @Value("${weather.api.key}")
    private String apiKey;
    @Value("${weather.api.url}")
    private String apiUrl;

    public Mono<JsonNode> fetchWeatherData(
        Integer x,
        Integer y
    ) {
        String baseDate = FormatUtil.formatDateKST(datePattern);
        String baseTime = FormatUtil.formatTimeKST(timePattern);

        // 미리 인코딩된 API 키를 그대로 사용하여 StringBuilder로 URI 생성
        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?")
                  .append("serviceKey=")
                  .append(apiKey);  // 미리 인코딩된 API 키 사용
        urlBuilder.append("&")
                  .append("pageNo=1");
        urlBuilder.append("&")
                  .append("numOfRows=1000");
        urlBuilder.append("&")
                  .append("dataType=JSON");
        urlBuilder.append("&")
                  .append("base_date=")
                  .append(baseDate);
        urlBuilder.append("&")
                  .append("base_time=")
                  .append(baseTime);
        urlBuilder.append("&")
                  .append("nx=")
                  .append(x);
        urlBuilder.append("&")
                  .append("ny=")
                  .append(y);

        URI uri = URI.create(urlBuilder.toString());
        System.out.println("uri = " + uri.toString());
        return webClient.get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .onErrorResume(e -> {
                            // 에러 처리 (예: 로깅 및 기본값 반환)
                            return Mono.empty();
                        });
    }
}
