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
}