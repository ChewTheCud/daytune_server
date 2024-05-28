package com.eumakase.eumakase.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Service
public class S3Service {
    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    /**
     * File 객체를 S3에 업로드
     * @param fileName 업로드할 파일의 이름
     * @param file 업로드할 파일 객체
     * @return 업로드된 파일의 CloudFront URL
     */
    public String uploadFile(String fileName, File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            // 파일 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            metadata.setContentType("audio/mpeg");

            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
        } catch (Exception e) {
            // 업로드 중 예외 발생 시 RuntimeException 발생
            throw new RuntimeException(e.getMessage());
        }

        // 업로드된 파일의 CloudFront URL 반환
        return "https://" + cloudFrontDomain + "/" + fileName;
    }

    /**
     * 바이트 배열을 S3에 업로드
     * @param fileName 업로드할 파일의 이름
     * @param content 업로드할 파일 내용 (바이트 배열)
     * @param contentType 파일의 콘텐츠 타입
     * @return 업로드된 파일의 CloudFront URL
     */
    public String uploadFile(String fileName, byte[] content, String contentType) {
        // 바이트 배열을 입력 스트림으로 변환
        InputStream inputStream = new ByteArrayInputStream(content);

        // 파일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        metadata.setContentType(contentType);

        // S3에 파일 업로드
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));

        // 업로드된 파일의 CloudFront URL 반환
        return "https://" + cloudFrontDomain + "/" + fileName;
    }
}