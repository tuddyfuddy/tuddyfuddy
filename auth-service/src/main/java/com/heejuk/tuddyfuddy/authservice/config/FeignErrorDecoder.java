package com.heejuk.tuddyfuddy.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.authservice.exception.FeignClientException;
import com.heejuk.tuddyfuddy.authservice.exception.FeignServerException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error occurred during feign client call: {} {}",
            methodKey, response.status());

        try {
            String errorBody = getErrorMessage(response);
            log.error("Error body: {}", errorBody);

            if (response.status() >= 400 && response.status() <= 499) {
                return new FeignClientException(
                    "Client error occurred during external service call: " + errorBody,
                    response.status()
                );
            }

            if (response.status() >= 500 && response.status() <= 599) {
                return new FeignServerException(
                    "Server error occurred during external service call: " + errorBody,
                    response.status()
                );
            }

            return new Exception("Unknown error occurred: " + errorBody);

        } catch (Exception e) {
            log.error("Error decoding response", e);
            return new Exception("Error processing error response");
        }
    }

    private String getErrorMessage(Response response) {
        try {
            if (response.body() == null) {
                return "No error body";
            }
            return new String(response.body().asInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading error message", e);
            return "Error reading error message";
        }
    }
}
