package com.heejuk.tuddyfuddy.userservice.dto.response;

import com.heejuk.tuddyfuddy.userservice.entity.User;
import java.util.List;

public record UserResponse(

    String id,
    String email,
    String nickname,
    String profileImage,
    String provider,
    List<String> roles

) {
    public static UserResponse of(User user) {
        return new UserResponse(
            String.valueOf(user.getId()),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImage(),
            user.getProvider(),
            user.getRoles()
        );
    }

}
