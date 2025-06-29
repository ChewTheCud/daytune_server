package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.Lyrics;
import com.eumakase.eumakase.dto.lyrics.LyricsCreateRequestDto;
import com.eumakase.eumakase.dto.lyrics.LyricsCreateResponseDto;
import com.eumakase.eumakase.dto.lyrics.LyricsPromptRequestDto;
import com.eumakase.eumakase.dto.lyrics.LyricsReadResponseDto;
import com.eumakase.eumakase.exception.LyricsException;
import com.eumakase.eumakase.repository.LyricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LyricsService {
    private final LyricsRepository lyricsRepository;
    private final ChatGPTService chatGPTService;

    /**
     * 일기 내용을 기반으로 가사 생성
     */
    public LyricsCreateResponseDto generateLyrics(LyricsCreateRequestDto dto) {
        // 1. 프롬프트 메시지 구성
        String systemMessage = "당신은 감성적인 한국어 작사가입니다. 사용자의 일기 내용을 바탕으로 한 편의 노래 가사를 6~12줄 내외로 써 주세요. 반드시 한국어로, 감정과 상황이 드러나게 써 주세요.";
        String userPrompt = dto.getDiaryContent();

        try {
            // ChatGPTService call (prompt 조립)
            String lyrics = chatGPTService.generateLyrics(dto.getMood(), dto.getDiaryContent());
            return LyricsCreateResponseDto.builder()
                    .content(lyrics)
                    .build();
        } catch (Exception e) {
            log.error("가사 생성 실패: {}", e.getMessage(), e);
            throw new LyricsException("가사 생성 실패: " + e.getMessage());
        }
    }

    public LyricsReadResponseDto readLyrics(Long lyricsId) {
        Lyrics lyrics = lyricsRepository.findById(lyricsId)
                .orElseThrow(() -> new LyricsException("Lyrics not found: " + lyricsId));
        return LyricsReadResponseDto.builder()
                .lyricsId(lyrics.getId())
                .musicId(lyrics.getMusic().getId())
                .content(lyrics.getContent())
                .build();
    }
}
