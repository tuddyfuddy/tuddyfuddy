package com.heejuk.tuddyfuddy.authservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ServerErrorException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error occurred during feign client call: {} {}",
            methodKey, response.status());

        if (response.status() >= 400 && response.status() <= 499) {
            return new ClientErrorException(
                "Client error occurred during external service call",
                response.status()
            );
        }

        if (response.status() >= 500 && response.status() <= 599) {
            return new ServerErrorException(
                "Server error occurred during external service call",
                response.status()
            );
        }

        return new Exception("Unknown error occurred");
    }
}
