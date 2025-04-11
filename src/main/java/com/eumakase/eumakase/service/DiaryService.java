package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.*;
import com.eumakase.eumakase.dto.chatGPT.PromptRequestDto;
import com.eumakase.eumakase.dto.chatGPT.PromptResponseDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateRequestDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateResponseDto;
import com.eumakase.eumakase.dto.diary.DiaryReadResponseDto;
import com.eumakase.eumakase.dto.diary.QuestionAnswerDto;
import com.eumakase.eumakase.dto.music.MusicCreateRequestDto;
import com.eumakase.eumakase.exception.DiaryException;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.repository.*;
import com.eumakase.eumakase.util.enums.PromptType;
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
    private final MusicRepository musicRepository;
    private final PromptCategoryRepository promptCategoryRepository;
    private final ChatGPTService chatGPTService;
    private final MusicService musicService;

    /**
     * 일기 생성
     * @param userId 사용자 ID
     * @param diaryCreateRequestDto 일기 생성 요청 DTO
     * @return 생성된 일기 응답 DTO
     */
    @Transactional
    public DiaryCreateResponseDto createDiary(Long userId, DiaryCreateRequestDto diaryCreateRequestDto) {
        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserException("해당하는 사용자를 찾을 수 없습니다."));

            PromptCategory promptCategory = promptCategoryRepository.findByMainPrompt(diaryCreateRequestDto.getEmotion());

            // 일기 엔티티 생성
            Diary diary = diaryCreateRequestDto.toEntity(user, promptCategory);

            // Diary 저장
            Diary savedDiary = diaryRepository.save(diary);

            // 질문-답변 최대 2개 저장
            List<DiaryQuestionAnswer> answers = IntStream.range(0, Math.min(2, diaryCreateRequestDto.getQuestionAnswers().size()))
                    .mapToObj(i -> {
                        QuestionAnswerDto qa = diaryCreateRequestDto.getQuestionAnswers().get(i);
                        return DiaryQuestionAnswer.builder()
                                .questionOrder(i + 1) // 순서: 1부터 시작
                                .diary(savedDiary)
                                .question(qa.getQuestion())
                                .answer(qa.getAnswer())
                                .build();
                    })
                    .collect(Collectors.toList());

            diaryQuestionAnswerRepository.saveAll(answers);

            // 생성된 일기 응답 DTO 반환
            return DiaryCreateResponseDto.of(savedDiary, answers);
        } catch (Exception e) {
            throw new DiaryException("Diary 생성 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    /**
     * 비동기 일기 생성 후처리
     * @param diaryId 일기 ID
     */
    @Async
    public void handleDiaryCreationAsync(Long diaryId) {
        try {
            // GPT 프롬프트 처리
            generatePromptAsync(diaryId);

            // 음악 생성
            generateMusicAsync(diaryId);
        } catch (Exception e) {
            log.error("Diary 추가 작업 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    /**
     * 비동기 GPT 프롬프트 생성
     * @param diaryId 일기 ID
     */
    @Async
    public void generatePromptAsync(Long diaryId) {
        try {
            // 일기 조회
            Diary diary = diaryRepository.findById(diaryId)
                    .orElseThrow(() -> new DiaryException("Diary not found with id: " + diaryId));

            // GPT를 사용하여 일기 내용 분석
            PromptRequestDto promptRequestDto = new PromptRequestDto(diary.getContent());
            PromptResponseDto promptResponseDto = chatGPTService.sendPrompt(promptRequestDto, PromptType.CONTENT_EMOTION_ANALYSIS);

            // promptCategoryId로 PromptCategory 조회
            Long promptCategoryId = diary.getPromptCategory().getId();
            PromptCategory promptCategory = promptCategoryRepository.findById(promptCategoryId)
                    .orElseThrow(() -> new RuntimeException("PromptCategory not found with id: " + promptCategoryId));

            // 일기 감정 값을 가져옴
            String emotion = promptCategory.getMainPrompt();

            // GPT로 생성한 내용을 Diary의 prompt 필드에 추가
            String updatedPrompt = emotion + ", " + (diary.getPrompt() != null ? diary.getPrompt() + ", " : "") + promptResponseDto.getContent();
            diary.setPrompt(updatedPrompt);

            // GPT를 사용하여 상담사 컨셉의 일기 Summary 생성
            PromptResponseDto counselorConceptResponseDto = chatGPTService.sendPrompt(promptRequestDto, PromptType.COUNSELOR_CONCEPT);

            // GPT로 생성한 내용을 Diary의 summary 필드에 추가
            diary.setSummary(counselorConceptResponseDto.getContent());

            // Diary 업데이트
            diaryRepository.save(diary);
        } catch (Exception e) {
            log.error("GPT 프롬프트 처리 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    /**
     * 비동기 음악 생성
     * @param diaryId 일기 ID
     */
    @Async
    public void generateMusicAsync(Long diaryId) {
        try {
            // 일기 조회
            Diary diary = diaryRepository.findById(diaryId)
                    .orElseThrow(() -> new DiaryException("Diary not found with id: " + diaryId));

            // 음악 생성 요청 DTO 생성 및 설정
            MusicCreateRequestDto musicCreateRequestDto = new MusicCreateRequestDto();
            musicCreateRequestDto.setDiaryId(diary.getId());
            musicCreateRequestDto.setGenerationPrompt(diary.getPrompt());  // GPT로 생성한 내용을 설정

            // 음악 생성
            musicService.createMusic(musicCreateRequestDto);
        } catch (Exception e) {
            log.error("음악 생성 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    /**
     * 일기 조회 (단일)
     * @param diaryId 일기 ID
     * @return 조회된 일기 응답 DTO
     */
    public DiaryReadResponseDto getDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary ID가 " + diaryId + "인 데이터를 찾을 수 없습니다."));

        List<DiaryQuestionAnswer> answers = diaryQuestionAnswerRepository.findByDiaryId(diaryId);

        // 해당 일기에 연결된 음악 URL을 가져옴 (첫 번째 음악 URL 사용)
        List<Music> musics = musicRepository.findByDiaryId(diaryId);
        // 음악이 생성되지 않았거나 음악을 아직 최종 선택하지 않았을 경우 Null 처리
        String musicUrl = musics.isEmpty() || musics.get(0).getFileUrl() == null || !musics.get(0).getFileUrl().startsWith("https://storage.googleapis.com") ? null : musics.get(0).getFileUrl();

        return DiaryReadResponseDto.of(diary, musicUrl, answers);
    }

    /**
     * 사용자의 모든 일기 조회
     * @param userId 사용자 ID
     * @return 일기 목록
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
                    // 음악이 생성되지 않았거나 음악을 아직 최종 선택하지 않았을 경우 Null 처리
                    String musicUrl = musics.isEmpty() || musics.get(0).getFileUrl() == null || !musics.get(0).getFileUrl().startsWith("https://storage.googleapis.com") ? null : musics.get(0).getFileUrl();
                    return DiaryReadResponseDto.of(diary, musicUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * 일기 삭제
     * @param diaryId 삭제할 일기 ID
     */
    public void deleteDiary(Long diaryId) {
        // Diary를 찾고 없으면 예외 발생
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary ID가 " + diaryId + "인 데이터를 찾을 수 없습니다."));

        // Diary 삭제
        diaryRepository.delete(diary);

        // 삭제 후 다시 조회하여 확인
        Optional<Diary> deletedDiary = diaryRepository.findById(diaryId);
        if (deletedDiary.isPresent()) {
            // 삭제 실패 시 예외 발생
            throw new DiaryException("Diary 삭제에 실패했습니다.");
        }
    }
}