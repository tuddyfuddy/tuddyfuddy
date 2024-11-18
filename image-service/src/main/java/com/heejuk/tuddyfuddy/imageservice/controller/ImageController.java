package com.heejuk.tuddyfuddy.imageservice.controller;

import com.heejuk.tuddyfuddy.imageservice.dto.CommonResponse;
import com.heejuk.tuddyfuddy.imageservice.dto.request.ImageCreateRequest;
import com.heejuk.tuddyfuddy.imageservice.dto.response.ImageAnalysisResponse;
import com.heejuk.tuddyfuddy.imageservice.service.ImageService;
import com.heejuk.tuddyfuddy.imageservice.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/images")
@Tag(name = "Image Analysis", description = "이미지 API")
public class ImageController {

    private final ImageService imageService;
    private final TranslationService translationService;

    @Operation(
        summary = "이미지 분석",
        description = "이미지를 받아 GPT-4 로 분석합니다."
    )
    @PostMapping(path = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<ImageAnalysisResponse> analyzeImageWithChatbot(
        @RequestPart("image") MultipartFile image
    ) {
        try {
            long startTime = System.currentTimeMillis();

            // 두 작업을 병렬로 실행
            CompletableFuture<String> descriptionFuture = CompletableFuture
                .supplyAsync(() -> {
                    long analysisStartTime = System.currentTimeMillis();
                    String result = imageService.analyzeImage(image);
                    long analysisEndTime = System.currentTimeMillis();
                    log.info("이미지 분석 소요시간: {}초", (analysisEndTime - analysisStartTime) / 1000.0);
                    return result;
                });

            CompletableFuture<String> imageUrlFuture = CompletableFuture
                .supplyAsync(() -> {
                    long uploadStartTime = System.currentTimeMillis();
                    String result = imageService.postImage(image);
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

    @Operation(
        summary = "이미지 생성",
        description = "GPT-4 에게 프롬프트를 넣어서 이미지를 생성해 S3에 넣고 해당 이미지 URL 을 반환합니다."
    )
    @PostMapping("/ai/gpt")
    public CommonResponse<String> createImage(
        @RequestBody ImageCreateRequest request
    ) {
        long startTime = System.currentTimeMillis();

        String imageUrl = imageService.createImage(request.text());

        long endTime = System.currentTimeMillis();
        log.info("전체 처리 소요시간: {}초", (endTime - startTime) / 1000.0);

        return CommonResponse.ok("이미지 생성 완료", imageUrl);
    }

    @Operation(
        summary = "이미지 생성 (갈덕이형꺼 API)",
        description = "???에게 프롬프트를 넣어서 이미지를 생성해 S3에 넣고 해당 이미지 URL 을 반환합니다."
    )
    @PostMapping("/ai/duck")
    public CommonResponse<String> createImage2(
        @RequestBody ImageCreateRequest request
    ) {
        long startTime = System.currentTimeMillis();

        String imageUrl = imageService.generateImage(request);

        long endTime = System.currentTimeMillis();
        log.info("전체 처리 소요시간: {}초", (endTime - startTime) / 1000.0);

        return CommonResponse.ok("이미지 생성 완료", imageUrl);
    }

    @Operation(
        summary = "이미지 생성 (번역 + 갈덕이형꺼 API)",
        description = "GPT 번역 후, ???에게 프롬프트를 넣어서 이미지를 생성해 S3에 넣고 해당 이미지 URL 을 반환합니다."
    )
    @PostMapping("/ai/mix")
    public CommonResponse<String> generateImageWithTranslation(
        @RequestBody ImageCreateRequest request
    ) {
        log.info("[이미지 생성 - 번역 전략]");
        long startTime = System.currentTimeMillis();
        log.info("번역 전 : {}", request.text());
        String translated = translationService.translateKoreanToEnglish(request.text());
        log.info("번역 후 : {}", translated);
        String imageUrl = imageService.generateImage(ImageCreateRequest.builder()
                                                                       .text(translated)
                                                                       .build());

        long endTime = System.currentTimeMillis();
        log.info("전체 처리 소요시간: {}초", (endTime - startTime) / 1000.0);

        return CommonResponse.ok("이미지 생성 완료", imageUrl);
    }
}
