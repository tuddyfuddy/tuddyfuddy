package com.heejuk.tuddyfuddy.contextservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.heejuk.tuddyfuddy.contextservice.config.KakaoApiConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakao-api", url = "${kakao.api.url}", configuration = KakaoApiConfig.class)
public interface KakaoApiClient {

    @GetMapping("/v2/local/geo/coord2regioncode.json")
    JsonNode fetchLocationData(
        @RequestParam("x") String longitude,
        @RequestParam("y") String latitude
    );
}
