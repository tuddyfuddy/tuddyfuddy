package com.heejuk.tuddyfuddy.imageservice.service;

import com.heejuk.tuddyfuddy.imageservice.client.ImageDownloadClient;
import com.heejuk.tuddyfuddy.imageservice.config.CustomMultipartFile;
import com.heejuk.tuddyfuddy.imageservice.util.S3Service;
import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final ChatClient chatClient;
    private final ImageDownloadClient imageDownloadClient;
    private final S3Service s3Service;
    private final OpenAiImageModel imageModel;

    public String analyzeImage(MultipartFile image) {
        try {
            // Resource로 변환
            Resource imageResource = new ByteArrayResource(image.getBytes());

            // 이미지 분석을 위한 프롬프트
            String prompt = """
                이 이미지를 간단 명료하게 분석해주세요.
                이미지에 보이는 주요 객체나 인물, 전반적인 분위기나 상황을 고려해서 한 짧은 묘사를 해주세요.
                주요 객체 한 줄로 분석.
                
                예를 들어, '산책하는 강아지'.
                이런식으로 한국어로 자연스럽고 간단하고 직관적으로 설명해주세요.
                마침표는 빼주세요.
                """;

            String response = chatClient.prompt()
                                        // prompt, image 넣기
                                        .user(u -> u.text(prompt)
                                                    .media(MimeTypeUtils.IMAGE_PNG, imageResource))
                                        .call()
                                        .content();

            return response;
        } catch (IOException e) {
            log.error("이미지 처리 중 오류 발생", e);
            throw new RuntimeException("이미지 처리 실패", e);
        }
    }

    public String postImage(MultipartFile image) {
        return s3Service.uploadImage(image);
    }

    public String createImage(String prompt) {
        try {
            ImageResponse response = imageModel.call(
                new ImagePrompt(prompt,
                                OpenAiImageOptions.builder()
                                                  .withModel("dall-e-3") // dall-e-3 모델
                                                  .withQuality("standard")
                                                  .withHeight(1024) // 최소가 1024x1024
                                                  .withWidth(1024)
                                                  .withN(1) // 이미지 한 개만 생성
                                                  .build())
            );

            String tempImageUrl = response.getResult()
                                          .getOutput()
                                          .getUrl();

            byte[] imageByteArray = downloadImage(tempImageUrl);

            MultipartFile multipartFile = new CustomMultipartFile(imageByteArray, "generated.png",
                                                                  "image/png");

            return s3Service.uploadImage(multipartFile);
        } catch (Exception e) {
            log.error("이미지 생성 중 오류 발생", e);
            throw new RuntimeException("이미지 생성 실패", e);
        }
    }

    /**
     * 해당 주소의 이미지 다운받기
     *
     * @param tempImageUrl
     * @return
     */
    private byte[] downloadImage(String tempImageUrl) {
        return imageDownloadClient.downloadImage(URI.create(tempImageUrl));
    }
}
