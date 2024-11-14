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
        log.error("Feign 클라이언트 호출 중 에러 발생: {} {}",
            methodKey, response.status());

        try {
            // 에러 응답 본문 읽기
            String errorBody = getErrorMessage(response);
            log.error("에러 응답 본문: {}", errorBody);

            // HTTP 상태 코드별 처리
            if (response.status() >= 400 && response.status() <= 499) {
                return new FeignClientException(
                    String.format("클라이언트 에러 발생 (%d): %s",
                        response.status(), errorBody),
                    response.status()
                );
            }

            if (response.status() >= 500 && response.status() <= 599) {
                return new FeignServerException(
                    String.format("서버 에러 발생 (%d): %s",
                        response.status(), errorBody),
                    response.status()
                );
            }

            return new Exception("알 수 없는 에러: " + errorBody);

        } catch (Exception e) {
            log.error("에러 응답 처리 중 예외 발생", e);
            return new Exception("에러 응답 처리 실패");
        }
    }

    private String getErrorMessage(Response response) throws IOException {
        if (response.body() == null) {
            return "응답 본문 없음";
        }
        return new String(response.body().asInputStream().readAllBytes(),
            StandardCharsets.UTF_8);
    }
}
