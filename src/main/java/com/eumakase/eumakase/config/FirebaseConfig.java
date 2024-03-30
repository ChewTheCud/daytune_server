package com.eumakase.eumakase.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Firebase 연동을 위한 환경 설정
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.bucket-name}")
    private String BUCKET_NAME;

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource("daytune-3722b-firebase-adminsdk-ee8uq-3ac1946af3.json").getInputStream()))
                    .setStorageBucket(BUCKET_NAME)
                    .build();
            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.error("Firebase 초기화 실패", e);
            throw new RuntimeException("Firebase를 초기화할 수 없습니다.", e);
        }
    }

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        return FirebaseMessaging.getInstance(initializeFirebaseApp());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
