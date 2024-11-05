package com.heejuk.tuddyfuddy.imageservice.service;

import com.heejuk.tuddyfuddy.imageservice.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImagePostService {

    private final S3Service s3Service;

    public String postImage(MultipartFile image) {
        return s3Service.uploadImage(image);
    }
}
