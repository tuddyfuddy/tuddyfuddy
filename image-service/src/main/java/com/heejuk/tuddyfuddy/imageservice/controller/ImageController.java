package com.heejuk.tuddyfuddy.imageservice.controller;

import com.heejuk.tuddyfuddy.imageservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.imageservice.dto.response.ImageAnalysisResponse;
import com.heejuk.tuddyfuddy.imageservice.service.ImageAnalysisService;
import com.heejuk.tuddyfuddy.imageservice.service.ImagePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        try {
            long startTime = System.currentTimeMillis();

            // 두 작업을 병렬로 실행
            CompletableFuture<String> descriptionFuture = CompletableFuture
                .supplyAsync(() -> {
                    long analysisStartTime = System.currentTimeMillis();
                    String result = imageAnalysisService.analyzeImage(image);
                    long analysisEndTime = System.currentTimeMillis();
                    log.info("이미지 분석 소요시간: {}초", (analysisEndTime - analysisStartTime) / 1000.0);
                    return result;
                });

            CompletableFuture<String> imageUrlFuture = CompletableFuture
                .supplyAsync(() -> {
                    long uploadStartTime = System.currentTimeMillis();
                    String result = imagePostService.postImage(image);
                    long uploadEndTime = System.currentTimeMillis();
                    log.info("이미지 업로드 소요시간: {}초", (uploadEndTime - uploadStartTime) / 1000.0);
                    return result;
                });

            // 두 작업이 모두 완료될 때까지 대기
            CompletableFuture.allOf(descriptionFuture, imageUrlFuture)
                             .join();

            // 결과 가져오기
            String description = descriptionFuture.get();
            String imageUrl = imageUrlFuture.get();

            long endTime = System.currentTimeMillis();
            log.info("전체 처리 소요시간: {}초", (endTime - startTime) / 1000.0);

            return CommonResponse.ok("이미지 분석 완료", ImageAnalysisResponse.builder()
                                                                       .imageUrl(imageUrl)
                                                                       .description(description)
                                                                       .build());

        } catch (InterruptedException | ExecutionException e) {
            log.error("이미지 처리 중 오류 발생", e);
            Thread.currentThread()
                  .interrupt();
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }
}
