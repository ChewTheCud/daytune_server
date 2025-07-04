package com.eumakase.eumakase.dto.lyrics;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * 가사 저장 요청 DTO (일기/음악 ID + 가사 내용)
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class LyricsCreateRequestDto implements Serializable {

    @NotNull(message = "일기 ID는 필수입니다.")
    private Long diaryId;

    @NotNull(message = "가사 내용은 필수입니다.")
    private String content;
}
