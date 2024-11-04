package com.heejuk.tuddyfuddy.authservice.service;

import static com.heejuk.tuddyfuddy.authservice.constant.JWT_SET.*;

import com.heejuk.tuddyfuddy.authservice.client.KakaoApiClient;
import com.heejuk.tuddyfuddy.authservice.client.UserServiceClient;
import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.authservice.dto.response.KakaoUserInfo;
import com.heejuk.tuddyfuddy.authservice.dto.response.UserResponse;
import com.heejuk.tuddyfuddy.authservice.exception.AuthenticationException;
import com.heejuk.tuddyfuddy.authservice.util.CookieUtil;
import com.heejuk.tuddyfuddy.authservice.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;
    private final KakaoApiClient kakaoApiClient;
    private final UserServiceClient userServiceClient;
    private final ReissueService refreshService;

    public void processKakaoLogin(String kakaoAccessToken, HttpServletResponse response) {
        try {
            KakaoUserInfo kakaoUserInfo = kakaoApiClient.getKakaoUserInfo(
                "Bearer " + kakaoAccessToken);
            CommonResponse<UserResponse> userResponse = userServiceClient.loginKakaoUser(
                kakaoUserInfo);

            if (userResponse.statusCode() != HttpStatus.OK.value()) {
                throw new AuthenticationException(
                    "Failed to process user data: " + userResponse.message());
            }

            UserResponse user = userResponse.result();

            // Access Token 생성
            String accessToken = jwtUtil.createJwt(
                "access",
                user.userId(),
                user.nickname(),
                ACCESS_TOKEN_EXPIRATION
            );

            // Refresh Token 생성
            String refreshToken = jwtUtil.createJwt(
                "refresh",
                user.userId(),
                user.nickname(),
                REFRESH_TOKEN_EXPIRATION
            );

            // Refresh Token을 Redis에 저장
            refreshService.saveRefreshToken(user.userId(), refreshToken);

            // Access Token을 헤더에 설정
            response.setHeader("Authorization", "Bearer " + accessToken);

            // Refresh Token을 쿠키에 설정
            Cookie refreshCookie = CookieUtil.createCookie("refresh_token", refreshToken);
            CookieUtil.addSameSiteCookieAttribute(response, refreshCookie);

        } catch (Exception e) {
            log.error("Error processing kakao login", e);
            throw new AuthenticationException("Failed to process kakao login", e);
        }
    }
}
