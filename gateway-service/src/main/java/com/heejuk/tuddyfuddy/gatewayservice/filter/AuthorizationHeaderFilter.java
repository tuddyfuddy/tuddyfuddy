package com.heejuk.tuddyfuddy.gatewayservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.gatewayservice.util.JWTUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends
    AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthorizationHeaderFilter(JWTUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.objectMapper = new ObjectMapper();
    }

    public static class Config {

    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // Authorization 헤더 확인
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(response, "Authorization 헤더가 없습니다.", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

            // Bearer 토큰 형식 확인
            if (!authorizationHeader.startsWith("Bearer ")) {
                return this.onError(response, "잘못된 토큰 형식입니다.", HttpStatus.UNAUTHORIZED);
            }

            // Bearer 제거
            String token = authorizationHeader.substring(7);

            try {
                jwtUtil.isExpired(token);
            } catch (Exception e) {
                return this.onError(response, "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
            }

            String category = jwtUtil.getCategory(token);
            if (!category.equals("access")) {
                return this.onError(response, "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
            }

            String userId = jwtUtil.getUserId(token);
            String nickname = jwtUtil.getNickname(token);

            log.info("Authorization header received userId: {}", userId);
            log.info("Authorization header received nickname: {}", nickname);

            // URL 인코딩된 닉네임
            String encodedNickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8);

            // 새로운 헤더 추가
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-UserId", userId)
                .header("X-Nickname", encodedNickname)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> onError(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        return response.writeWith(Mono.just(response.bufferFactory()
            .wrap(writeValueAsBytes(errorResponse))));
    }

    private byte[] writeValueAsBytes(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("Error writing error response", e);
            return "{\"error\": \"Internal server error\"}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
