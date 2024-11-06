package com.heejuk.tuddyfuddy.imageservice.controller;

import com.heejuk.tuddyfuddy.imageservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.imageservice.dto.response.ImageAnalysisResponse;
import com.heejuk.tuddyfuddy.imageservice.service.ImageAnalysisService;
import com.heejuk.tuddyfuddy.imageservice.service.ImagePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/images/analysis")
@Tag(name = "Image Analysis", description = "이미지 분석 API")
public class ImageController {

    private final ChatClient chatClient;
    private final ImageAnalysisService imageAnalysisService;
    private final ImagePostService imagePostService;

    @Operation(
        summary = "이미지 분석",
        description = "이미지를 받아 GPT-4 로 분석합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<ImageAnalysisResponse> analyzeImageWithChatbot(
        @RequestPart("image") MultipartFile image
    ) {
        String description = imageAnalysisService.analyzeImage(image);
        String imageUrl = imagePostService.postImage(image);

        return CommonResponse.ok("이미지 분석 완료", ImageAnalysisResponse.builder()
                                                                   .imageUrl(imageUrl)
                                                                   .description(description)
                                                                   .build());
    }
}
