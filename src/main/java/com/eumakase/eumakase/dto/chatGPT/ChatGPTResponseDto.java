package com.eumakase.eumakase.dto.chatGPT;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 프롬프트 생성 응답 DTO
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class ChatGPTResponseDto implements Serializable {
    private List<Choice> choices;
    private String model; // 실제 처리 모델 (미터링 단가 매칭용)
    private Usage usage; // 토큰 사용량 (미터링용)
}
