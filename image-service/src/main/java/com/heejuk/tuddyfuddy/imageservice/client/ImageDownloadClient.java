package com.heejuk.tuddyfuddy.imageservice.client;

import com.heejuk.tuddyfuddy.imageservice.config.FeignConfig;
import java.net.URI;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "image-download-client", url = "", configuration = FeignConfig.class)
public interface ImageDownloadClient {

    @GetMapping
    byte[] downloadImage(URI uri);
}
