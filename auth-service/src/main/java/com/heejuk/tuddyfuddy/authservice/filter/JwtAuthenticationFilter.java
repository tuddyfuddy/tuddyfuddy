package com.heejuk.tuddyfuddy.authservice.filter;

import com.heejuk.tuddyfuddy.authservice.exception.InvalidTokenException;
import com.heejuk.tuddyfuddy.authservice.exception.TokenExpiredException;
import com.heejuk.tuddyfuddy.authservice.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null) {
                validateToken(token);
                Authentication authentication = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (TokenExpiredException e) {

            log.error("Token has expired", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;

        } catch (Exception e) {
            log.error("Error processing JWT token", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void validateToken(String token) {
        // 토큰이 refresh 타입인지 확인
        if ("refresh".equals(jwtUtil.getCategory(token))) {
            throw new InvalidTokenException("Refresh token is not allowed for authentication");
        }

        // 토큰 만료 확인
        if (jwtUtil.isExpired(token)) {
            throw new TokenExpiredException("Token has expired");
        }
    }

    private Authentication getAuthentication(String token) {
        String userId = jwtUtil.getUserId(token);
        return new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
    }
}

