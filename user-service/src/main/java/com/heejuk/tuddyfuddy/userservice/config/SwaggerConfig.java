package com.heejuk.tuddyfuddy.userservice.config;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.servers.*;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.*;

@Configuration
@OpenAPIDefinition(servers = @Server(url = "/", description = "Default Server URL"))
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("사용자 API")
                .version("1.0")
                .description("사용자 API 문서"));
    }
}