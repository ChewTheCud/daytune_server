package com.eumakase.eumakase.dto.lyrics;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * 가사 생성 요청 DTO (일기 내용 + 분위기)
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class LyricsPromptRequestDto implements Serializable {
    @NotNull(message = "일기 내용은 필수입니다.")
    private String diaryContent;

    @NotNull(message = "원하는 분위기는 필수입니다.")
    private String mood;
}
