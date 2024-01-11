package com.eumakase.eumakase.dto.ChatGPT;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프롬프트 답변 DTO
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class PromptResponseDto {
    private String content;

    public PromptResponseDto(String content) {
        this.content = content;
    }
}
