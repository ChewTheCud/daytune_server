package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.service.FCMService;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * FCM Push API
 */
@RestController
@RequestMapping(value = "/api/v1/push")
public class PushController {
    private final FCMService fcmService;

    public PushController(FCMService fcmService) {
        this.fcmService = fcmService;
    }

    /**
     * 음악 파일 URL 업데이트
     *
     * @return
     */
    @PostMapping("test")
    public ResponseEntity<ApiResponse<Object>> sendPushNotification() throws IOException, FirebaseMessagingException {
        try {
            fcmService.sendPushNotification("Music 생성 완료", "당신의 Diary에 모든 Music 파일이 성공적으로 추가되었습니다.", "fcmToken temp");
            return ResponseEntity.ok(ApiResponse.success("Push알림을 정상적으로 전송했습니다."));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Push 알림 전송에 실패했습니다."));
        }
    }
}