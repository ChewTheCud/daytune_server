package com.eumakase.eumakase.dto.chatGPT;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String prompt;
}
