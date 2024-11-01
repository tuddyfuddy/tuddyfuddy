package com.heejuk.tuddyfuddy.userservice.service;

import com.heejuk.tuddyfuddy.userservice.dto.request.KakaoUserInfo;
import com.heejuk.tuddyfuddy.userservice.dto.response.UserResponse;
import com.heejuk.tuddyfuddy.userservice.entity.User;
import com.heejuk.tuddyfuddy.userservice.exception.UserNotFoundException;
import com.heejuk.tuddyfuddy.userservice.repository.UserRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
        String email = request.kakaoAccount().email();

        // 기존 회원인지 확인
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createKakaoUser(request));

        return UserResponse.of(user);
    }

    private User createKakaoUser(KakaoUserInfo request) {
        return userRepository.save(
            User.builder()
                .kakaoId(request.id())
                .email(request.kakaoAccount().email())
                .nickname(request.kakaoAccount().profile().nickname())
                .profileImage(request.kakaoAccount().profile().profileImageUrl())
                .birthdate(combineBirthDate(request.kakaoAccount().birthday(),
                    request.kakaoAccount().birthyear()))
                .build()
        );
    }

    public UserResponse getUser(String userId) {
        User user = userRepository.findById(Long.valueOf(userId))
            .orElseThrow(UserNotFoundException::new);

        return UserResponse.of(user);
    }

    private LocalDate combineBirthDate(String birthday, String birthyear) {
        if (birthday == null || birthday.length() != 4) {
            return null;
        }

        String year = birthyear != null ? birthyear : "2000";
        String month = birthday.substring(0, 2);
        String day = birthday.substring(2, 4);

        try {
            return LocalDate.parse(year + "-" + month + "-" + day);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse birth date", e);
            return null;
        }
    }

}