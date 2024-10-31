package com.heejuk.tuddyfuddy.contextservice.mapper;

import com.heejuk.tuddyfuddy.contextservice.dto.request.HealthRequest;
import com.heejuk.tuddyfuddy.contextservice.dto.response.HealthResponse;
import com.heejuk.tuddyfuddy.contextservice.entity.Health;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthMapper {

    Health toEntity(HealthRequest dto);

    HealthResponse toDto(Health entity);

    List<HealthResponse> toDtoList(List<Health> entities);
}
