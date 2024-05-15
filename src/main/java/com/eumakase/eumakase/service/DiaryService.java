package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.chatGPT.PromptRequestDto;
import com.eumakase.eumakase.dto.chatGPT.PromptResponseDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateRequestDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateResponseDto;
import com.eumakase.eumakase.dto.diary.DiaryReadResponseDto;
import com.eumakase.eumakase.dto.music.MusicCreateRequestDto;
import com.eumakase.eumakase.exception.DiaryException;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.util.enums.PromptType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DiaryService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final ChatGPTService chatGPTService;
    private final MusicService musicService;

    public DiaryService(UserRepository userRepository, DiaryRepository diaryRepository, ChatGPTService chatGPTService, MusicService musicService) {
        this.userRepository = userRepository;
        this.diaryRepository = diaryRepository;
        this.chatGPTService = chatGPTService;
        this.musicService = musicService;
    }

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

            // 일기 엔티티 생성
            Diary diary = diaryCreateRequestDto.toEntity(diaryCreateRequestDto, user);

            // Diary 저장
            Diary savedDiary = diaryRepository.save(diary);

            // 생성된 일기 응답 DTO 반환
            return DiaryCreateResponseDto.of(savedDiary);
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

            // GPT로 생성한 내용을 Diary의 prompt 필드에 추가
            String updatedPrompt = diary.getPrompt() + " , " + promptResponseDto.getContent();
            diary.setPrompt(updatedPrompt);

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
        return DiaryReadResponseDto.of(diary);
    }

    /**
     * 사용자의 모든 일기 조회
     * @param userId 사용자 ID
     * @return 일기 목록
     */
    public List<Diary> getAllDiariesByUserId(Long userId) {
        List<Diary> diaries = diaryRepository.findByUserId(userId);
        if (diaries.isEmpty()) {
            throw new DiaryException("User ID가 " + userId + "인 Diary 데이터를 찾을 수 없습니다.");
        }
        return diaries;
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