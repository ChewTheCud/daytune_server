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
    private String generationPrompt;
    private Long promptCategoryId;
}