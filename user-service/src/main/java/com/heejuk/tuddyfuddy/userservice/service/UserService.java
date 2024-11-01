package com.heejuk.tuddyfuddy.userservice.service;

import com.heejuk.tuddyfuddy.userservice.dto.request.KakaoUserInfo;
import com.heejuk.tuddyfuddy.userservice.dto.request.KakaoUserInfo.KakaoAccount;
import com.heejuk.tuddyfuddy.userservice.dto.request.KakaoUserInfo.KakaoAccount.Profile;
import com.heejuk.tuddyfuddy.userservice.dto.response.UserResponse;
import com.heejuk.tuddyfuddy.userservice.entity.User;
import com.heejuk.tuddyfuddy.userservice.exception.UserNotFoundException;
import com.heejuk.tuddyfuddy.userservice.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse processKakaoUser(KakaoUserInfo request) {
        log.info("Processing Kakao user info: {}", request);

        KakaoAccount kakaoAccount = Optional.ofNullable(request.kakaoAccount())
            .orElseThrow(
                () -> new IllegalArgumentException("KakaoAccount information is required"));

        Profile profile = Optional.ofNullable(kakaoAccount.profile())
            .orElseThrow(() -> new IllegalArgumentException("Profile is required"));

        String nickname = Optional.ofNullable(profile.nickname())
            .orElseThrow(() -> new IllegalArgumentException("Nickname is required"));

        User user = userRepository.findByKakaoId(request.id())
            .map(existingUser -> updateUser(existingUser, profile))
            .orElseGet(() -> createUser(request.id(), profile));

        return UserResponse.of(user);
    }

    private User createUser(Long kakaoId, Profile profile) {
        return userRepository.save(User.builder()
            .kakaoId(kakaoId)
            .nickname(profile.nickname())
            .profileImage(profile.profileImageUrl())
            .build());
    }

    private User updateUser(User user, Profile profile) {
        user.updateProfile(profile.nickname(), profile.profileImageUrl());
        return user;
    }

    public UserResponse getUser(String userId) {
        User user = userRepository.findById(Long.valueOf(userId))
            .orElseThrow(UserNotFoundException::new);

        return UserResponse.of(user);
    }
}