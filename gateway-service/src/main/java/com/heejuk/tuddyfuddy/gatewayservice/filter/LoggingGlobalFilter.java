package com.heejuk.tuddyfuddy.gatewayservice.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    ) {
        ServerHttpRequest request = exchange.getRequest();

        // 요청 메소드, 경로, 특정 헤더 로깅
        String token = request.getHeaders()
                              .getFirst("Authorization"); // 필요에 따라 다른 헤더 이름 사용 가능
        log.info("Incoming request: method={}, uri={}, token={}",
                 request.getMethod(), request.getURI(), token);

        // 라우트된 URL 로깅
        return chain.filter(exchange)
                    .doOnSuccess(aVoid -> {
                        Object routedUrl = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
                        if (routedUrl != null) {
                            log.info("Routed to URL: {}", routedUrl);
                        } else {
                            log.warn("Routed URL not found in attributes.");
                        }
                    });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
