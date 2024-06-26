package com.eumakase.eumakase.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;

@Service
public class FirebaseService {
    @Value("${firebase.bucket-name}")
    private String bucketName;

    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);

    public String uploadFile(MultipartFile file) throws Exception {
        String fileName = "music/" + System.currentTimeMillis() + "_" + file.getOriginalFilename(); // 저장될 파일 이름 (경로 포함)

        // 파일 업로드
        StorageClient.getInstance().bucket().create(fileName, file.getInputStream(), file.getContentType());

        // 업로드된 파일의 다운로드 URL 반환
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }


    public List<String> getFileDownloadUrlsByDiaryId(String diaryId) {
        List<String> downloadUrls = new ArrayList<>();

        // StorageOptions을 사용하여 인증 및 설정을 다른 코드 부분에서 이미 완료했음을 가정
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // 'music/' 디렉토리 내의 객체를 필터링하기 위해 지정된 접두사 정의
        String prefix = "music/" + diaryId + "_"; // 더 효율적인 필터링을 위해 diaryId를 접두사에 직접 포함

        // 버킷에서 지정된 접두사를 가진 객체를 나열
        for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll()) {
            try {
                // 각 파일의 다운로드 URL 생성
                String encodedBlobName = URLEncoder.encode(blob.getName(), "UTF-8");
                String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + encodedBlobName + "?alt=media";

                log.info("Download URL: {}", downloadUrl);

                downloadUrls.add(downloadUrl);
            } catch (Exception e) {
                log.error("Blob의 다운로드 URL 생성 오류: {}", blob.getName(), e);
            }
        }

        log.info("Download API Test");
        return downloadUrls;
    }

    /**
     * 선택된 음악 파일을 Firebase Storage에 업로드
     * @return 업로드된 파일의 Firebase Storage URL
     */
    public String uploadFileToFirebaseStorage(File file, String diaryId) {
        try {
            // Firebase Storage에 파일을 업로드
            Bucket bucket = StorageClient.getInstance().bucket();
            String blobName = "music/diaryId_" + diaryId + ".mp3"; // 파일명
            Blob blob = bucket.create(blobName, new FileInputStream(file), "audio/mpeg");

            // Firebase Storage URL을 반환
            return blob.getMediaLink();
        } catch (IOException e) {
            throw new RuntimeException("Firebase Storage에 파일 업로드 중 오류가 발생했습니다.", e);
        }
    }
}