package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.Music;
import com.eumakase.eumakase.domain.PromptCategory;
import com.eumakase.eumakase.dto.music.MusicCreateRequestDto;
import com.eumakase.eumakase.exception.MusicException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class MusicService {

    private final MusicRepository musicRepository;
    private final DiaryRepository diaryRepository;

    public MusicService(MusicRepository musicRepository, DiaryRepository diaryRepository) {
        this.musicRepository = musicRepository;
        this.diaryRepository = diaryRepository;
    }

    /**
     * Music 생성
     */
    @Transactional
    public void createMusic(MusicCreateRequestDto musicCreateRequestDto) {
        Diary diary = diaryRepository.findById(musicCreateRequestDto.getDiaryId())
                .orElseThrow(() -> new IllegalArgumentException("Diary not found with id: " + musicCreateRequestDto.getDiaryId()));

        PromptCategory promptCategory = null;

        //TODO: promptCategoryRepository 구현 필요
//        if (requestDto.getPromptCategoryId() != null) {
//            promptCategory = promptCategoryRepository.findById(requestDto.getPromptCategoryId())
//                    .orElseThrow(() -> new IllegalArgumentException("PromptCategory not found with id: " + requestDto.getPromptCategoryId()));
//        }

        Music music = Music.builder()
                .diary(diary)
                .promptCategory(promptCategory)
                .build();

        musicRepository.save(music);
    }

    /**
     * Music 삭제
     */
    public void deleteMusic(Long musicId) {
        // Music을 찾고 없으면 예외 발생
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new MusicException("Music ID가 " + musicId + "인 데이터를 찾을 수 없습니다."));

        // Diary 삭제
        musicRepository.delete(music);

        // 삭제 후 다시 조회하여 확인
        Optional<Music> deletedMusic = musicRepository.findById(musicId);
        if (deletedMusic.isPresent()) {
            // 삭제 실패 시 예외 발생
            throw new MusicException("Music 삭제에 실패했습니다.");
        }
    }
}