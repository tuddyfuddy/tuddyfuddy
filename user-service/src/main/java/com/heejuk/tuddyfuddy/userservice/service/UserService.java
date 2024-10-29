package com.heejuk.tuddyfuddy.userservice.service;

import com.heejuk.tuddyfuddy.userservice.dto.request.KakaoInfoRequest;
import com.heejuk.tuddyfuddy.userservice.dto.response.UserResponse;
import com.heejuk.tuddyfuddy.userservice.entity.User;
import com.heejuk.tuddyfuddy.userservice.exception.UserNotFoundException;
import com.heejuk.tuddyfuddy.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse processKakaoUser(KakaoInfoRequest request) {
        String email = request.kakaoAccount().email();

        // 기존 회원인지 확인
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createKakaoUser(request));

        return UserResponse.of(user);
    }

    private User createKakaoUser(KakaoInfoRequest request) {
        return userRepository.save(User.builder()
            .email(request.kakaoAccount().email())
            .nickname(request.properties().nickname())
            .profileImage(request.properties().profileImage())
            .provider("KAKAO")
            .providerId(String.valueOf(request.id()))
            .build());
    }

    public UserResponse getUser(String userId) {
        User user = userRepository.findById(Long.valueOf(userId))
            .orElseThrow(UserNotFoundException::new);

        return UserResponse.of(user);
    }
}