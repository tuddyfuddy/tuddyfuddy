package com.heejuk.tuddyfuddy.userservice.repository;

import com.heejuk.tuddyfuddy.userservice.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByKakaoId(Long kakaoId);

}
