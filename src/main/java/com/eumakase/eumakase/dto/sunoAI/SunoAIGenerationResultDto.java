package com.eumakase.eumakase.dto.sunoAI;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Data
public class SunoAIGenerationResultDto implements Serializable {
    private List<String> sunoAiMusicIds;

    public SunoAIGenerationResultDto(List<String> sunoAiMusicIds) {
        this.sunoAiMusicIds = sunoAiMusicIds;
    }
}