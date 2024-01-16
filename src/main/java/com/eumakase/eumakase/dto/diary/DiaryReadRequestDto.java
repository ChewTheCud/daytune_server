package com.eumakase.eumakase.dto.diary;

import lombok.*;

import java.io.Serializable;

/**
 * Diary 생성 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class DiaryReadRequestDto implements Serializable {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String mood;
}