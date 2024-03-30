package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.FirebaseConfig;
import com.eumakase.eumakase.domain.FCMToken;
import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.repository.FCMTokenRepository;
import com.eumakase.eumakase.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FCMService {
    private final FirebaseConfig firebaseConfig;
    private final FirebaseMessaging firebaseMessaging;
    private final FCMTokenRepository fcmTokenRepository;

    private final UserRepository userRepository;

    public FCMService(FirebaseConfig firebaseConfig, FirebaseMessaging firebaseMessaging, FCMTokenRepository fcmTokenRepository, UserRepository userRepository) {
        this.firebaseConfig = firebaseConfig;
        this.firebaseMessaging = firebaseMessaging;
        this.fcmTokenRepository = fcmTokenRepository;
        this.userRepository = userRepository;
    }

    public void sendPushNotification(String title, String message, String fcmToken) throws IOException, FirebaseMessagingException {
        // FCM 메시지 구성
        Message fcmMessage = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .build())
                .setToken(fcmToken) // FCM 토큰 사용
                .build();
        firebaseMessaging.send(fcmMessage);
    }

    // FCM 토큰 저장 또는 업데이트
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        FCMToken token = fcmTokenRepository.findByUser(user)
                .orElseGet(FCMToken::new);
        token.setUser(user);
        token.setFcmToken(fcmToken);
        fcmTokenRepository.save(token);
    }
}

