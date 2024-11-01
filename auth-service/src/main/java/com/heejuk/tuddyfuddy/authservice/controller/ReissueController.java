package com.heejuk.tuddyfuddy.authservice.controller;

import static com.heejuk.tuddyfuddy.authservice.constant.JWT_SET.*;

import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.authservice.exception.InvalidTokenException;
import com.heejuk.tuddyfuddy.authservice.exception.TokenExpiredException;
import com.heejuk.tuddyfuddy.authservice.service.ReissueService;
import com.heejuk.tuddyfuddy.authservice.util.CookieUtil;
import com.heejuk.tuddyfuddy.authservice.util.JWTUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "인증 관리", description = "refresh API 입니다.")
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final ReissueService reissueService;

    @PostMapping("/reissue")
    public CommonResponse<?> reissueToken(
        @CookieValue(name = "refresh_token") String refreshToken,
        HttpServletResponse response
    ) {
        // 리프레시 토큰 검증
        if (jwtUtil.isExpired(refreshToken)) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String savedToken = reissueService.findRefreshToken(userId.toString())
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!savedToken.equals(refreshToken)) {
            throw new InvalidTokenException("Refresh token mismatch");
        }

        // 새로운 액세스 토큰 발급
        String newAccessToken = jwtUtil.createJwt(
            "access",
            userId,
            jwtUtil.getNickname(refreshToken),
            ACCESS_TOKEN_EXPIRATION
        );

        // 새로운 리프레시 토큰 발급
        String newRefreshToken = jwtUtil.createJwt(
            "refresh",
            userId,
            jwtUtil.getNickname(refreshToken),
            REFRESH_TOKEN_EXPIRATION
        );

        // Redis 업데이트
        reissueService.deleteRefreshToken(userId.toString());
        reissueService.saveRefreshToken(userId.toString(), newRefreshToken);

        // 헤더와 쿠키 설정
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        Cookie refreshCookie = CookieUtil.createCookie("refresh_token", newRefreshToken);
        CookieUtil.addSameSiteCookieAttribute(response, refreshCookie);

        return CommonResponse.ok("토큰이 재발급되었습니다.");
    }
}
