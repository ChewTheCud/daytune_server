package com.eumakase.eumakase.dto.diary;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 알가 감정분석 결과 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class EmotionInsightResponseDto implements Serializable {
    private List<EmotionInsightDto> emotions;
}
