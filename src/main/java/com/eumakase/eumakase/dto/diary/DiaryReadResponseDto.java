package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Diary 생성 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class DiaryReadResponseDto implements Serializable {
    private Long id;
    private Long userId;
    private String content;
    private String summary;
    private String musicUrl;
    private LocalDateTime createdDate;

    public static DiaryReadResponseDto of(Diary diary, String musicUrl) {
        return DiaryReadResponseDto.builder()
                .id(diary.getId())
                .userId(diary.getUser() != null ? diary.getUser().getId() : null)
                .content(diary.getContent())
                .summary(diary.getSummary())
                .musicUrl(musicUrl)
                .createdDate(diary.getCreatedDate())
                .build();
    }
}