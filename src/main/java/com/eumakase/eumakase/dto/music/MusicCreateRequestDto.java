// 수정된 MusicCreateRequestDto.java
package com.eumakase.eumakase.dto.music;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.Music;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * Music 생성 요청 DTO
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MusicCreateRequestDto implements Serializable {

    @NotNull
    private Long diaryId;

    @NotNull
    private String generationPrompt;

    @NotNull
    private String style;

    @NotNull
    private String title;

    private String negativeTags;
}
