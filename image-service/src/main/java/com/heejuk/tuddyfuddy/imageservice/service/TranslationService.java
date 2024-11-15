package com.heejuk.tuddyfuddy.imageservice.service;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final ChatClient chatClient;

    public String translateKoreanToEnglish(String message) {
        String prompt = String.format("""
                                          다음 문장을 한국어에서 영어로 번역해줘.
                                          번역된 문장으로 이미지 생성하게 할 거니까
                                          이미지 생성 잘 되도록 신경써서 번역해줘.
                                          다른 미사여구 없이 번역한 말만 다시 주면돼.
                                          
                                          번역할 문장: %s
                                          """, message);

        String translatedPrompt = chatClient.prompt()
                                            .user(prompt)
                                            .call()
                                            .content();
        return translatedPrompt;
    }
}
