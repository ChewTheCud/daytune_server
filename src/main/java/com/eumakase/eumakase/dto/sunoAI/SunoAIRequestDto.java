package com.eumakase.eumakase.dto.sunoAI;

import com.eumakase.eumakase.config.SunoAIConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Music 생성 요청 DTO
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SunoAIRequestDto implements Serializable {
    private String prompt;

    @JsonProperty("make_instrumental")
    @Builder.Default
    private String makeInstrumental = SunoAIConfig.MAKE_INSTRUMENTAL;

    public SunoAIRequestDto(String gptDescriptionPrompt) {
        this.prompt = gptDescriptionPrompt;
        this.makeInstrumental = SunoAIConfig.MAKE_INSTRUMENTAL;
    }
}