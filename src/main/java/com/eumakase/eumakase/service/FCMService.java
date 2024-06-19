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
import java.util.Optional;

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

    public void sendPushNotification(String title, String message, String fcmToken) throws FirebaseMessagingException {
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

    /**
     * 사용자 ID로 FCM 토큰 조회
     * @param userId 사용자 ID
     * @return FCM 토큰
     */
    public String getFcmTokenByUserId(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(userId + " userId를 가진 사용자를 찾을 수 없습니다."));

        // FCM 토큰 조회
        Optional<FCMToken> tokenOptional = fcmTokenRepository.findByUser(user);
        if (tokenOptional.isEmpty()) {
            throw new RuntimeException("User ID: " + userId + "에 대한 FCM 토큰을 찾을 수 없습니다.");
        }
        FCMToken token = tokenOptional.get();
        return token.getFcmToken();
    }

    /**
     * 사용자 ID로 사용자 닉네임 조회
     * @param userId 사용자 ID
     * @return 사용자 닉네임
     */
    public String getUserNicknameByUserId(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(userId + " userId를 가진 사용자를 찾을 수 없습니다."));
        return user.getNickname();
    }
}

