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
    @NotNull
    private String prompt;

    @NotNull
    private String style;

    @NotNull
    private String title;

    @JsonProperty("customMode")
    @Builder.Default
    private boolean customMode = SunoAIConfig.DEFAULT_CUSTOM_MODE;

    @JsonProperty("instrumental")
    @Builder.Default
    private boolean instrumental = SunoAIConfig.DEFAULT_INSTRUMENTAL;

    @Builder.Default
    private String model = SunoAIConfig.DEFAULT_MODEL;

    @JsonProperty("negativeTags")
    private String negativeTags;

    @JsonProperty("callBackUrl")
    @Builder.Default
    private String callBackUrl = SunoAIConfig.DEFAULT_CALLBACK_URL;
}
