package com.heejuk.tuddyfuddy.userservice.controller;

import com.heejuk.tuddyfuddy.userservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.userservice.dto.request.KakaoUserInfo;
import com.heejuk.tuddyfuddy.userservice.dto.response.UserResponse;
import com.heejuk.tuddyfuddy.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "회원 관리", description = "회원 API 입니다.")
public class UserController {

    private final UserService userService;

    @PostMapping("/kakao")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카카오 사용자 생성/수정", description = "카카오 사용자 정보로 회원가입 또는 정보 업데이트를 수행합니다.")
    public CommonResponse<UserResponse> createOrUpdateKakaoUser(@RequestBody KakaoUserInfo request) {
        log.info("Received request to create or update kakao user with id: {}", request.id());

        UserResponse userResponse = userService.processKakaoUser(request);

        return CommonResponse.ok("카카오 사용자 정보 처리 완료", userResponse);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "회원 조회")
    public CommonResponse<?> getUser(@RequestHeader HttpHeaders headers) {

        String userId = Objects.requireNonNull(headers.get("X-UserId")).get(0);

        UserResponse response = userService.getUser(userId);

        return CommonResponse.ok("회원 조회 성공", response);
    }

}