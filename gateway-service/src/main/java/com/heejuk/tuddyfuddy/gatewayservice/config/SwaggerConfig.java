package com.heejuk.tuddyfuddy.gatewayservice.config;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.servers.*;
import org.springdoc.core.models.*;
import org.springframework.context.annotation.*;

@Configuration
@OpenAPIDefinition(servers = @Server(url = "/", description = "Default Server URL"))
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi authServiceApi() {
        return GroupedOpenApi.builder()
            .group("auth-service")
            .pathsToMatch("/auth/**")
            .build();
    }

    @Bean
    public GroupedOpenApi chatServiceApi() {
        return GroupedOpenApi.builder()
            .group("chat-service")
            .pathsToMatch("/chat/**")
            .build();
    }

    @Bean
    public GroupedOpenApi healthServiceApi() {
        return GroupedOpenApi.builder()
            .group("health-service")
            .pathsToMatch("/health/**")
            .build();
    }

    @Bean
    public GroupedOpenApi notificationServiceApi() {
        return GroupedOpenApi.builder()
            .group("notification-service")
            .pathsToMatch("/notification/**")
            .build();
    }

    @Bean
    public GroupedOpenApi openaiApiProxyApi() {
        return GroupedOpenApi.builder()
            .group("openai-api-proxy")
            .pathsToMatch("/openai-api-proxy/**")
            .build();
    }

    @Bean
    public GroupedOpenApi userServiceApi() {
        return GroupedOpenApi.builder()
            .group("user-service")
            .pathsToMatch("/user/**")
            .build();
    }

}