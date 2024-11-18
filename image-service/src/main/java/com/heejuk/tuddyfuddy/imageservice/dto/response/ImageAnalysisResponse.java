package com.heejuk.tuddyfuddy.imageservice.dto.response;

import lombok.Builder;

@Builder
public record ImageAnalysisResponse(
    String description,
    String imageUrl
) {

}
