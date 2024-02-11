package com.eumakase.eumakase.service;

import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
public class FileService {
    @Value("${firebase.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws Exception {
        String fileName = "music/" + System.currentTimeMillis() + "_" + file.getOriginalFilename(); // 저장될 파일 이름 (경로 포함)

        // 파일 업로드
        StorageClient.getInstance().bucket().create(fileName, file.getInputStream(), file.getContentType());

        // 업로드된 파일의 다운로드 URL 반환
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }
}