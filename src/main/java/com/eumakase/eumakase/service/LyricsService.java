package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.Lyrics;
import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.dto.lyrics.LyricsCreateRequestDto;
import com.eumakase.eumakase.dto.lyrics.LyricsCreateResponseDto;
import com.eumakase.eumakase.dto.lyrics.LyricsPromptRequestDto;
import com.eumakase.eumakase.dto.lyrics.LyricsPromptResponseDto;
import com.eumakase.eumakase.dto.lyrics.LyricsReadResponseDto;
import com.eumakase.eumakase.exception.LyricsException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.LyricsRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LyricsService {
    private final LyricsRepository lyricsRepository;
    private final DiaryRepository diaryRepository;
    private final ChatGPTService chatGPTService;

    /**
     * 1. [프롬프트] 일기 + 분위기로 임시 가사 생성 (저장 X)
     */
    public LyricsPromptResponseDto generatePromptLyrics(LyricsPromptRequestDto lyricsPromptRequestDto) {
        try {
            String lyrics = chatGPTService.generateLyrics(lyricsPromptRequestDto.getDiaryContent(), lyricsPromptRequestDto.getMood());
            return LyricsPromptResponseDto.builder()
                    .content(lyrics)
                    .build();
        } catch (Exception e) {
            log.error("가사 프롬프트 생성 실패: {}", e.getMessage(), e);
            throw new LyricsException("가사 프롬프트 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 2. 실제 DB에 가사 저장
     */
    @Transactional
    public LyricsCreateResponseDto saveLyrics(LyricsCreateRequestDto lyricsCreateRequestDto) {
        log.info("[saveLyrics 호출] diaryId={} / musicId={} / content='{}'",
                lyricsCreateRequestDto.getDiaryId(),
                lyricsCreateRequestDto.getContent()
        );

        // Diary, Music 유효성 체크 및 조회
        Diary diary = diaryRepository.findById(lyricsCreateRequestDto.getDiaryId())
                .orElseThrow(() -> new LyricsException("Diary ID가 " + lyricsCreateRequestDto.getDiaryId() + "인 데이터를 찾을 수 없습니다."));

        // Lyrics 엔티티 생성 및 저장
        Lyrics lyrics = Lyrics.builder()
                .diary(diary)
                .content(lyricsCreateRequestDto.getContent())
                .build();

        try {
            lyricsRepository.save(lyrics);
            log.info("[saveLyrics 완료] lyricsId={}", lyrics.getId());
        } catch (Exception e) {
            log.error("Lyrics 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new LyricsException("가사 저장에 실패했습니다: " + e.getMessage());
        }

        return LyricsCreateResponseDto.builder()
                .lyricsId(lyrics.getId())
                .content(lyrics.getContent())
                .build();
    }

    /**
     * 3. 특정 일기의 음악 가사 조회 (diaryId → lyrics 1건)
     */
    public LyricsReadResponseDto getLyricsByDiaryId(Long diaryId) {
        Lyrics lyrics = lyricsRepository.findByDiaryId(diaryId);
        if (lyrics == null) {
            throw new LyricsException("해당 일기의 가사를 찾을 수 없습니다. diaryId=" + diaryId);
        }
        return LyricsReadResponseDto.builder()
                .lyricsId(lyrics.getId())
                .content(lyrics.getContent())
                .build();
    }
}
