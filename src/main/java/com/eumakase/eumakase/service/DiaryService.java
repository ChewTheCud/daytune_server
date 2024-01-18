package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.dto.diary.DiaryCreateRequestDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateResponseDto;
import com.eumakase.eumakase.dto.diary.DiaryReadResponseDto;
import com.eumakase.eumakase.exception.DiaryException;
import com.eumakase.eumakase.repository.DiaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public DiaryService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    /**
     * Diary 생성
     */
    @Transactional
    public DiaryCreateResponseDto createDiary(DiaryCreateRequestDto diaryCreateRequestDto) {
        try {
            // DiaryCreateRequestDto 객체를 Diary 엔티티로 변환
            Diary diary = diaryCreateRequestDto.toEntity(diaryCreateRequestDto);

            // Diary 저장
            Diary savedDiary = diaryRepository.save(diary);

            // DiaryCreateResponseDto 객체 생성 및 반환
            return DiaryCreateResponseDto.of(savedDiary);
        } catch (Exception e) {
            // 예외 처리 로직
            throw new DiaryException("Diary 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * Diary 조회 (단일)
     */
    public DiaryReadResponseDto getDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary ID가 " + diaryId + "인 데이터를 찾을 수 없습니다."));

        return DiaryReadResponseDto.of(diary);
    }

    /**
     * Diary 삭제
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