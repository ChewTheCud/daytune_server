package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.*;
import com.eumakase.eumakase.dto.diary.*;
import com.eumakase.eumakase.dto.music.MusicCreateRequestDto;
import com.eumakase.eumakase.exception.DiaryException;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiaryService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryQuestionAnswerRepository diaryQuestionAnswerRepository;
    private final DiaryEmotionInsightRepository diaryEmotionInsightRepository;
    private final MusicRepository musicRepository;
    private final PromptCategoryRepository promptCategoryRepository;
    private final ChatGPTService chatGPTService;
    private final MusicService musicService;

    /**
     * 일기 생성
     */
    @Transactional
    public DiaryCreateResponseDto createDiary(Long userId, DiaryCreateRequestDto requestDto) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserException("해당하는 사용자를 찾을 수 없습니다."));

            PromptCategory promptCategory = promptCategoryRepository.findByMainPrompt(requestDto.getMainEmotion());

            Diary diary = requestDto.toEntity(user, promptCategory);
            Diary savedDiary = diaryRepository.save(diary);

            List<DiaryQuestionAnswer> answers = IntStream.range(0, Math.min(2, requestDto.getQuestionAnswers().size()))
                    .mapToObj(i -> {
                        QuestionAnswerDto qa = requestDto.getQuestionAnswers().get(i);
                        return DiaryQuestionAnswer.builder()
                                .questionOrder(i + 1)
                                .diary(savedDiary)
                                .question(qa.getQuestion())
                                .answer(qa.getAnswer())
                                .build();
                    })
                    .collect(Collectors.toList());

            List<DiaryEmotionInsight> emotions = requestDto.getEmotions().stream().limit(3).map(emotionInsightDto -> DiaryEmotionInsight.builder()
                    .diary(savedDiary)
                    .emotion(emotionInsightDto.getEmotion())
                    .reason(emotionInsightDto.getReason())
                    .build())
                    .collect(Collectors.toList());

            diaryQuestionAnswerRepository.saveAll(answers);
            diaryEmotionInsightRepository.saveAll(emotions);
            return DiaryCreateResponseDto.of(savedDiary, answers, emotions);

        } catch (Exception e) {
            throw new DiaryException("Diary 생성 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    /**
     * 비동기 일기 생성 후처리
     */
    @Async
    public void handleDiaryCreationAsync(Long diaryId) {
        try {
            chatGPTService.updateDiarySummary(diaryId);
            generateMusicAsync(diaryId);
        } catch (Exception e) {
            log.error("Diary 추가 작업 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    /**
     * 비동기 음악 생성
     */
    @Async
    public void generateMusicAsync(Long diaryId) {
        try {
            Diary diary = diaryRepository.findById(diaryId)
                    .orElseThrow(() -> new DiaryException("Diary not found with id: " + diaryId));

            MusicCreateRequestDto musicCreateRequestDto = new MusicCreateRequestDto();
            musicCreateRequestDto.setDiaryId(diary.getId());
            musicCreateRequestDto.setGenerationPrompt(diary.getPrompt());

            musicService.createMusic(musicCreateRequestDto);
        } catch (Exception e) {
            log.error("음악 생성 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    /**
     * 일기 조회 (단일)
     */
    public DiaryReadResponseDto getDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary ID가 " + diaryId + "인 데이터를 찾을 수 없습니다."));

        List<DiaryQuestionAnswer> answers = diaryQuestionAnswerRepository.findByDiaryId(diaryId);
        List<DiaryEmotionInsight> emotions = diaryEmotionInsightRepository.findByDiaryId(diaryId);
        List<Music> musics = musicRepository.findByDiaryId(diaryId);
        String musicUrl = musics.isEmpty() || musics.get(0).getFileUrl() == null || !musics.get(0).getFileUrl().startsWith("https://storage.googleapis.com")
                ? null : musics.get(0).getFileUrl();

        return DiaryReadResponseDto.of(diary, musicUrl, answers, emotions);
    }

    /**
     * 사용자 전체 일기 조회
     */
    @Transactional
    public List<DiaryReadResponseDto> getAllDiariesByUserId(Long userId) {
        List<Diary> diaries = diaryRepository.findByUserId(userId);
        if (diaries.isEmpty()) {
            throw new DiaryException("User ID가 " + userId + "인 Diary 데이터를 찾을 수 없습니다.");
        }
        return diaries.stream()
                .map(diary -> {
                    List<Music> musics = musicRepository.findByDiaryId(diary.getId());
                    String musicUrl = musics.isEmpty() || musics.get(0).getFileUrl() == null || !musics.get(0).getFileUrl().startsWith("https://storage.googleapis.com")
                            ? null : musics.get(0).getFileUrl();
                    return DiaryReadResponseDto.of(diary, musicUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * 일기 삭제
     */
    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary ID가 " + diaryId + "인 데이터를 찾을 수 없습니다."));

        diaryRepository.delete(diary);

        Optional<Diary> deletedDiary = diaryRepository.findById(diaryId);
        if (deletedDiary.isPresent()) {
            throw new DiaryException("Diary 삭제에 실패했습니다.");
        }
    }
}