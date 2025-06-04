package com.eumakase.eumakase.dto.sunoAI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Suno AI /generate 응답을 매핑하기 위한 DTO
 * GenerateResponseDto를 SunoAIGenerationResultDto로 변경한 형태입니다.
 */
@Data
public class SunoAIGenerationResultDto {
    private int code;
    private String msg;
    private GenerateData data;

    @Data
    public static class GenerateData {
        @JsonProperty("taskId")
        private String taskId;
    }
}
