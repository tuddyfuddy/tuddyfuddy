package com.heejuk.tuddyfuddy.gatewayservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public GroupedOpenApi contextServiceApi() {
        return GroupedOpenApi.builder()
                             .group("context-service")
                             .pathsToMatch("/context/**")
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
    public GroupedOpenApi logServiceApi() {
        return GroupedOpenApi.builder()
                             .group("log-service")
                             .pathsToMatch("/log-service/**")
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