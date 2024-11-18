package com.heejuk.tuddyfuddy.imageservice.dto.request;

import lombok.Builder;

@Builder
public record ImageCreateRequest(
    String text
) {

}
