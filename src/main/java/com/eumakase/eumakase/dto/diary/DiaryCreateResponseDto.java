package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.DiaryEmotionInsight;
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
    private List<EmotionInsightDto> emotions;

    public static DiaryCreateResponseDto of(Diary diary,  List<DiaryQuestionAnswer> questionAnswers, List<DiaryEmotionInsight> emotions) {
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
                .emotions(
                        emotions.stream()
                                .map(emotion -> new EmotionInsightDto(
                                        emotion.getEmotion(),
                                        emotion.getReason()
                                ))
                                .collect(Collectors.toList())
                )
                .build();
    }
}