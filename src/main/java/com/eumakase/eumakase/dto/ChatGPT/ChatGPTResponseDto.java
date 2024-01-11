package com.eumakase.eumakase.dto.ChatGPT;

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
}
