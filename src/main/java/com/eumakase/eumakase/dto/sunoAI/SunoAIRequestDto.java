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
    private boolean makeInstrumental = SunoAIConfig.MAKE_INSTRUMENTAL;

    @JsonProperty("wait_audio")
    @Builder.Default
    private boolean waitAudio = SunoAIConfig.WAIT_AUDIO;

    public SunoAIRequestDto(String gptDescriptionPrompt) {
        this.prompt = gptDescriptionPrompt;
    }
}