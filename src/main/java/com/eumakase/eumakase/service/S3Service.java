package com.eumakase.eumakase.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class S3Service {
    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @Value("${cloud.aws.cloudfront.key-pair-id}")
    private String cloudFrontKeyPairId;

    @Value("${cloud.aws.cloudfront.private-key-file}")
    private String cloudFrontPrivateKeyFile;

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

//    /**
//     * CloudFront 서명된 URL 생성
//     * @param fileName 서명할 파일의 이름
//     * @param expirationMinutes URL의 유효시간(분)
//     * @return 서명된 URL
//     */
//    public String generateSignedUrl(String fileName, int expirationMinutes) {
//        try {
//            // 유효기간 설정
//            Date expiration = Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));
//
//            // CloudFront private key 파일 로드
//            ClassPathResource resource = new ClassPathResource(cloudFrontPrivateKeyFile);
//            File privateKeyFile = resource.getFile();
//            System.out.println("Using private key file: " + privateKeyFile.getAbsolutePath());
//
//            // 서명된 URL 생성
//            String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
//                    SignerUtils.Protocol.https,
//                    cloudFrontDomain,
//                    privateKeyFile,
//                    "/" + fileName,
//                    cloudFrontKeyPairId,
//                    expiration);
//
//            System.out.println(signedUrl);
//            return signedUrl;
//        } catch (Exception e) {
//            e.printStackTrace();  // 예외 스택 트레이스를 출력합니다.
//            throw new RuntimeException("서명된 URL 생성 중 오류 발생", e);
//        }
//    }
}