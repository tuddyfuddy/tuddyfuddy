package com.heejuk.tuddyfuddy.contextservice.repository;

import com.heejuk.tuddyfuddy.contextservice.entity.Health;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthRepository extends MongoRepository<Health, Long> {

    List<Health> findByUserIdOrderByTimestampDesc(Long userId);

    List<Health> findByUserIdAndTimestampBetween(
        String userId,
        LocalDateTime start,
        LocalDateTime end
    );
}
