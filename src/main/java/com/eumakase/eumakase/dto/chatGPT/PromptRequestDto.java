package com.eumakase.eumakase.dto.chatGPT;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 프롬프트 질문 요청시 DTO
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class PromptRequestDto implements Serializable {
    private String prompt;
}
