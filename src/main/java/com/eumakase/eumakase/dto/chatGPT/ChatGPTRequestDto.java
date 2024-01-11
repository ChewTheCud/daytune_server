package com.eumakase.eumakase.dto.chatGPT;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 프롬프트 생성 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class ChatGPTRequestDto implements Serializable {
    private String model;

    private List<Message> messages;
    private Double temperature;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    @JsonProperty("top_p")
    private Double topP;
    @JsonProperty("n")
    private Integer choiceNumber;

    public ChatGPTRequestDto(String model, List<Message> messages, Double temperature, Integer maxTokens, Double topP, Integer choiceNumber) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.choiceNumber = choiceNumber;
    }
}
