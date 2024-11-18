package com.heejuk.tuddyfuddy.authservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = new ObjectMapper().writeValueAsString(
            CommonResponse.forbidden("접근 권한이 없습니다")
        );
        response.getWriter().write(body);
    }
}
