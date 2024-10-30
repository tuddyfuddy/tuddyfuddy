package com.heejuk.tuddyfuddy.contextservice.repository;

import com.heejuk.tuddyfuddy.contextservice.entity.*;
import java.time.*;
import java.util.*;
import org.springframework.data.mongodb.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface HealthRepository extends MongoRepository<Health, Long> {

    List<Health> findByUserIdOrderByTimestampDesc(Long userId);

    List<Health> findByUserIdAndTimestampBetween(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );
}
