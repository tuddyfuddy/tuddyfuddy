package com.heejuk.tuddyfuddy.notificationservice.repository;

import com.heejuk.tuddyfuddy.notificationservice.entity.AiInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiInfoRepository extends JpaRepository<AiInfo, Long> {

    Optional<AiInfo> findByAiName(String aiName);

}
