package com.heejuk.tuddyfuddy.gatewayservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.gatewayservice.util.JWTUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
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

            if (!request.getHeaders().containsKey("access")) {
                return this.onError(response, "access이 없습니다.", HttpStatus.UNAUTHORIZED);
            }

            String accessToken = request.getHeaders().get("access").get(0);

            try {
                jwtUtil.isExpired(accessToken);
            } catch (Exception e) {
                return this.onError(response, "access 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
            }

            String category = jwtUtil.getCategory(accessToken);

            if (!category.equals("access")) {
                return this.onError(response, "Invalid access token", HttpStatus.UNAUTHORIZED);
            }

            Long userId = jwtUtil.getUserId(accessToken);
            String nickname = jwtUtil.getNickname(accessToken);

            log.info("Authorization header received userId: {}", userId);
            log.info("Authorization header received nickname: {}", nickname);

            String encodedString;
            encodedString = URLEncoder.encode(nickname, StandardCharsets.UTF_8);

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-UserId", String.valueOf(userId))
                .header("X-Nickname", encodedString)
                .build();
            exchange = exchange.mutate().request(modifiedRequest).build();

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerHttpResponse response, String errorMessage,
        HttpStatus httpStatus) {
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);

        return Mono.defer(() -> {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
            } catch (JsonProcessingException e) {
                byte[] fallbackMessage = "{\"error\": \"Internal server error\"}".getBytes(
                    StandardCharsets.UTF_8);
                return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(fallbackMessage)));
            }
        });
    }
}
