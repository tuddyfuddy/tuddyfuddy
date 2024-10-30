package com.heejuk.tuddyfuddy.authservice.config;

import static com.heejuk.tuddyfuddy.authservice.constant.SECURITY_SET.*;
import static com.heejuk.tuddyfuddy.authservice.constant.CORS_SET.*;

import com.heejuk.tuddyfuddy.authservice.handler.CustomAccessDeniedHandler;
import com.heejuk.tuddyfuddy.authservice.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
        throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            )

            .cors(cors -> cors
                .configurationSource(configurationSource())
            )

            .csrf((csrf) -> csrf
                .disable()
            )

            .formLogin((loginForm) -> loginForm.
                disable()
            )

            .httpBasic((basic) -> basic
                .disable()
            )

            .authorizeHttpRequests((auth) -> auth
                .requestMatchers(PERMITALL_URL_PATTERNS).permitAll()
                .requestMatchers(NEED_LOGIN_URL_PATTERNS).authenticated()
                .requestMatchers(NEED_ADMIN_ROLE_URL_PATTERNS).hasRole("ADMIN")
                .anyRequest().permitAll()
            )

            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .exceptionHandling((exceptions) -> exceptions
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource configurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(ALLOWED_ORIGINS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowCredentials(ALLOWED_CREDENTIALS);
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        configuration.setMaxAge(MAX_AGE_1H);

        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();
        corsConfigSource.registerCorsConfiguration("/**", configuration);

        return corsConfigSource;
    }

}
