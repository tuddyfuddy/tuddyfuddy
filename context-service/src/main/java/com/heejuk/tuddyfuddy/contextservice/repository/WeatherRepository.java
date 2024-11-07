package com.heejuk.tuddyfuddy.contextservice.repository;

import com.heejuk.tuddyfuddy.contextservice.entity.Weather;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends MongoRepository<Weather, Long> {

}
