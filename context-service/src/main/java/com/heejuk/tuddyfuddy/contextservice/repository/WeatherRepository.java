package com.heejuk.tuddyfuddy.contextservice.repository;

import com.heejuk.tuddyfuddy.contextservice.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends MongoRepository<Weather, Long> {

    List<Weather> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start,
        LocalDateTime end
    );

    Optional<Weather> findFirstByXAndYOrderByTimestampDesc(
        Integer x,
        Integer y
    );
}
