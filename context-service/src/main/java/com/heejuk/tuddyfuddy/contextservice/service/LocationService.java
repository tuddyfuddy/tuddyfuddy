package com.heejuk.tuddyfuddy.contextservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.heejuk.tuddyfuddy.contextservice.client.KakaoApiClient;
import com.heejuk.tuddyfuddy.contextservice.exception.KakaoApiRequestException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class LocationService {

    private final KakaoApiClient kakaoApiClient;

    public String getLocation(
        String longitude,
        String latitude
    ) {
        try {
            JsonNode jsonNode = kakaoApiClient.fetchLocationData(longitude, latitude);
            return getRegion(jsonNode);
        } catch (FeignException e) {
            log.error("Failed to fetch weather data at longitude:{}, latitude:{}, error: {}",
                      longitude,
                      latitude,
                      e.getMessage());
            throw new KakaoApiRequestException("Failed to fetch kakao location data");
        }
    }

    private String getRegion(JsonNode jsonNode) {
        JsonNode documents = jsonNode.get("documents");
        if (documents.isEmpty()) {
            throw new KakaoApiRequestException("No location data found in Kakao API response.");
        }
        JsonNode document = documents.get(0);
        String sido = document.get("region_1depth_name")
                              .asText();
        String gugun = document.get("region_2depth_name")
                               .asText();
        
        log.info("Fetched location data: sido = {}, gugun = {}", sido, gugun);

        // 서울은 구 반환
        if (sido.contains("서울")) {
            return gugun.split(" ")[0];
        } else { // 그 외는 시도 반환
            return sido;
        }
    }
}
