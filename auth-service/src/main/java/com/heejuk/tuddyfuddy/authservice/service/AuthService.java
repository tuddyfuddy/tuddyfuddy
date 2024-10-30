package com.heejuk.tuddyfuddy.authservice.service;

import com.heejuk.tuddyfuddy.authservice.client.UserServiceClient;
import com.heejuk.tuddyfuddy.authservice.config.JwtTokenProvider;
import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.authservice.dto.KakaoUserInfo;
import com.heejuk.tuddyfuddy.authservice.dto.response.TokenResponse;
import com.heejuk.tuddyfuddy.authservice.dto.response.UserResponse;
import com.heejuk.tuddyfuddy.authservice.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RestTemplate restTemplate;
    private final UserServiceClient userServiceClient;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse processKakaoLogin(String kakaoAccessToken) {
        try {
            KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);

            // Feign client를 통해 User 서비스 호출
            CommonResponse<UserResponse> userResponse = userServiceClient.createOrUpdateKakaoUser(kakaoUserInfo);

            if (userResponse.statusCode() != HttpStatus.OK.value()) {
                throw new AuthenticationException("Failed to process user data: " + userResponse.message());
            }

            String accessToken = jwtTokenProvider.createToken(userResponse.result().id());
            return new TokenResponse(accessToken);
        } catch (Exception e) {
            log.error("Error processing kakao login", e);
            throw new AuthenticationException("Failed to process kakao login", e);
        }
    }

    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                KakaoUserInfo.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            throw new AuthenticationException("Failed to get Kakao user info", e);
        }
    }
}
