package com.heejuk.tuddyfuddy.imageservice.client;

import com.heejuk.tuddyfuddy.imageservice.config.FeignConfig;
import com.heejuk.tuddyfuddy.imageservice.dto.request.ImageCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "image-create-ai-client", url = "http://222.107.238.75:5100/", configuration = FeignConfig.class)
public interface ImageCreateAiClient {

    @PostMapping
    Object createImage(@RequestBody ImageCreateRequest data);
}
