package com.heejuk.tuddyfuddy.authservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = new ObjectMapper().writeValueAsString(
            CommonResponse.unauthorized("인증에 실패했습니다")
        );
        response.getWriter().write(body);
    }
}
