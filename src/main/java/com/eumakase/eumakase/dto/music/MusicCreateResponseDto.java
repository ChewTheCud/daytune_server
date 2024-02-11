package com.eumakase.eumakase.dto.music;

import com.eumakase.eumakase.domain.Music;
import lombok.*;

import java.io.Serializable;

/**
 * Music 생성 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class MusicCreateResponseDto implements Serializable {
    private Long id;
    private Long diaryId;
    private Long promptCategoryId;
    private String promptCategoryTitle;

    public static MusicCreateResponseDto of(Music music) {
        return MusicCreateResponseDto.builder()
                .id(music.getId())
                .diaryId(music.getDiary().getId())
                .promptCategoryId(music.getPromptCategory() != null ? music.getPromptCategory().getId() : null)
                .promptCategoryTitle(music.getPromptCategory() != null ? music.getPromptCategory().getTitle() : null)
                .build();
    }
}