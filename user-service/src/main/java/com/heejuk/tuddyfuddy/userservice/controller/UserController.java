package com.heejuk.tuddyfuddy.userservice.controller;

import com.heejuk.tuddyfuddy.userservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.userservice.dto.request.KakaoInfoRequest;
import com.heejuk.tuddyfuddy.userservice.dto.response.UserResponse;
import com.heejuk.tuddyfuddy.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "회원 관리", description = "회원 API 입니다.")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "로그인 및 회원가입")
    public CommonResponse<?> processKakaoUser(@RequestBody KakaoInfoRequest request) {

        UserResponse response = userService.processKakaoUser(request);

        return CommonResponse.ok("카카오 로그인 성공", response);
    }

}