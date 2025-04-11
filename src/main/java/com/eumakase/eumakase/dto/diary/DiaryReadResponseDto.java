package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.DiaryQuestionAnswer;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private String emotion;
    private String content;
    private List<QuestionAnswerDto> questionAnswers;
    private String summary;
    private String musicUrl;
    private LocalDateTime createdDate;

    public static DiaryReadResponseDto of(Diary diary, String musicUrl, List<DiaryQuestionAnswer> questionAnswers) {
        return DiaryReadResponseDto.builder()
                .id(diary.getId())
                .userId(diary.getUser() != null ? diary.getUser().getId() : null)
                .emotion(diary.getPromptCategory() != null ? diary.getPromptCategory().getMainPrompt() : null)
                .content(diary.getContent())
                .questionAnswers(
                        questionAnswers.stream()
                                .map(qa -> new QuestionAnswerDto(
                                        qa.getQuestionOrder(),
                                        qa.getQuestion(),
                                        qa.getAnswer()
                                ))
                                .collect(Collectors.toList())
                )
                .summary(diary.getSummary())
                .musicUrl(musicUrl)
                .createdDate(diary.getCreatedDate())
                .build();
    }

    public static DiaryReadResponseDto of(Diary diary, String musicUrl) {
        return DiaryReadResponseDto.of(diary, musicUrl, List.of());
    }
}