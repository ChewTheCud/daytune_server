package com.eumakase.eumakase.dto.chatGPT;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 응답 데이터 내 usage 키 값 내부 (토큰 사용량)
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class Usage implements Serializable {
    @JsonProperty("prompt_tokens")
    private Long promptTokens;

    @JsonProperty("completion_tokens")
    private Long completionTokens;

    @JsonProperty("total_tokens")
    private Long totalTokens;
}
