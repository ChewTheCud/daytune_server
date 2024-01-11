package com.eumakase.eumakase.dto.ChatGPT;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 요청&응답 데이터 내 messages 키 값 내부
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class Message {
    private String role;
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}