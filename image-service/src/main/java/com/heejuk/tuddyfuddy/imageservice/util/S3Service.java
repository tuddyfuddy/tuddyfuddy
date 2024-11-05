package com.heejuk.tuddyfuddy.imageservice.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    // 허용된 파일 확장자 목록
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png",
                                                                         ".gif");

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    public String uploadImage(MultipartFile file) {
        try {
            // 파일 이름 생성 (UUID + 원본 파일명)
            String originName = file.getOriginalFilename();
            String fileName = createFileName(originName);
            String extension = getFileExtension(originName);

            // 확장자 검증
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new RuntimeException("지원하지 않는 파일 형식입니다: " + extension);
            }

            // PutObjectRequest 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                                .bucket(bucketName)
                                                                .key(fileName)
                                                                .contentType(file.getContentType())
                                                                .build();

            // RequestBody 생성 및 업로드
            s3Client.putObject(putObjectRequest,
                               RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // S3 URL 반환
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    // 파일 이름 생성 (UUID + 원본 파일명)
    private String createFileName(String originalFileName) {
        // uuid 10글자
        String uuid = UUID.randomUUID()
                          .toString()
                          .substring(0, 10);
        // 날짜
        String timestamp = LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 파일명에서 공백과 특수문자 제거
        String cleanFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "");
        // 날짜로 구분
        return String.format("%s/%s_%s", timestamp, uuid, cleanFileName);
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new RuntimeException("잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }
}
