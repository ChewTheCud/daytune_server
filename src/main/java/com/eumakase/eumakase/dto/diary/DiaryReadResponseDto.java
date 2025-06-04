package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.DiaryEmotionInsight;
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
    private String mainEmotion;
    private List<QuestionAnswerDto> questionAnswers;
    private List<EmotionInsightDto> emotions;
    @Builder.Default
    private boolean musicStatus = false; // diary에 음악이 모두 생성/업데이트되어 있으면 true
    private String musicUrl;
    private LocalDateTime createdDate;

    public static DiaryReadResponseDto of(Diary diary, boolean musicStatus, String musicUrl, List<DiaryQuestionAnswer> questionAnswers, List<DiaryEmotionInsight> emotions) {
        return DiaryReadResponseDto.builder()
                .id(diary.getId())
                .userId(diary.getUser() != null ? diary.getUser().getId() : null)
                .mainEmotion(diary.getPromptCategory() != null ? diary.getPromptCategory().getMainPrompt() : null)
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
                .musicStatus(musicStatus)
                .musicUrl(musicUrl)
                .createdDate(diary.getCreatedDate())
                .build();
    }

    public static DiaryReadResponseDto of(Diary diary, String musicUrl) {
        return DiaryReadResponseDto.of(diary, false, musicUrl, List.of(), List.of());
    }
}