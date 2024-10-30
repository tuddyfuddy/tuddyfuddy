package com.heejuk.tuddyfuddy.authservice.controller;

import com.heejuk.tuddyfuddy.authservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.authservice.dto.request.KakaoLoginRequest;
import com.heejuk.tuddyfuddy.authservice.dto.response.TokenResponse;
import com.heejuk.tuddyfuddy.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "인증 관리", description = "인증 API 입니다.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카카오 로그인", description = "카카오 액세스 토큰으로 로그인을 수행합니다.")
    public CommonResponse<?> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {

        log.info("Kakao login request received with code: {}", request);
        TokenResponse response = authService.processKakaoLogin(request.accessToken());

        return CommonResponse.ok("카카오 로그인 성공", response);
    }

}