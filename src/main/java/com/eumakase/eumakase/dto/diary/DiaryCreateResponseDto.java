package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.DiaryQuestionAnswer;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Diary 생성 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class DiaryCreateResponseDto implements Serializable {
    private Long id;
    private String emotion;
    private String content;
    private List<QuestionAnswerDto> questionAnswers;

    public static DiaryCreateResponseDto of(Diary diary,  List<DiaryQuestionAnswer> questionAnswers) {
        return DiaryCreateResponseDto.builder()
                .id(diary.getId())
                .emotion(diary.getPromptCategory().getMainPrompt())
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
                .build();
    }
}