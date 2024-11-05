package com.heejuk.tuddyfuddy.imageservice.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageAnalysisService {

    private final ChatClient chatClient;

    public String analyzeImage(MultipartFile image) {
        try {
            // Resource로 변환
            Resource imageResource = new ByteArrayResource(image.getBytes());

            // 이미지 분석을 위한 프롬프트
            String prompt = """
                이 이미지를 간단 명료하게 분석해주세요.
                이미지에 보이는 주요 객체나 인물, 전반적인 분위기나 상황을 고려해서 한 짧은 묘사를 해주세요.

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
}
