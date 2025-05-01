package com.eumakase.eumakase.dto.diary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 일기 분석 내용 (감정-이유)
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EmotionInsightDto {
    @NotBlank
    private String emotion;

    @NotBlank
    private String reason;
}
