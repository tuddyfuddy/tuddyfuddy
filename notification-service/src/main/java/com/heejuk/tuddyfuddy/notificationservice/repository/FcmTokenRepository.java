package com.heejuk.tuddyfuddy.notificationservice.repository;

import com.heejuk.tuddyfuddy.notificationservice.entity.FcmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByUserId(String userId);

}
