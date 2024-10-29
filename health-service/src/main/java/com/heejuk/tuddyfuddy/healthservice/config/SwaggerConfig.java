package com.heejuk.tuddyfuddy.healthservice.config;

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
            .info(new Info().title("생체 데이터 API")
                .version("1.0")
                .description("생체 데이터 API 문서"));
    }
}