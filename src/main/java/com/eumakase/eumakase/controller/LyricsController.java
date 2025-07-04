package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.lyrics.LyricsCreateRequestDto;
import com.eumakase.eumakase.dto.lyrics.LyricsCreateResponseDto;
import com.eumakase.eumakase.dto.lyrics.LyricsPromptRequestDto;
import com.eumakase.eumakase.dto.lyrics.LyricsPromptResponseDto;
import com.eumakase.eumakase.exception.LyricsException;
import com.eumakase.eumakase.service.LyricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v2/lyrics")
@RequiredArgsConstructor
public class LyricsController {
    private final LyricsService lyricsService;

    /**
     * 가사 프롬프트 생성(저장X, 임시 미리보기)
     */
    @PostMapping("/prompt")
    public ResponseEntity<ApiResponse<LyricsPromptResponseDto>> promptLyrics(
            @Valid @RequestBody LyricsPromptRequestDto lyricsPromptRequestDto) {
        try {
            LyricsPromptResponseDto lyricsPromptResponseDto = lyricsService.generatePromptLyrics(lyricsPromptRequestDto);
            return ResponseEntity.ok(ApiResponse.success("가사 프롬프트 생성 성공", lyricsPromptResponseDto));
        } catch (LyricsException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("가사 프롬프트 생성에 실패했습니다."));
        }
    }

    /**
     * 실제 가사 저장(DB 반영)
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<LyricsCreateResponseDto>> saveLyrics(
            @Valid @RequestBody LyricsCreateRequestDto lyricsCreateRequestDto) {
        try {
            LyricsCreateResponseDto responseDto = lyricsService.saveLyrics(lyricsCreateRequestDto);
            return ResponseEntity.ok(ApiResponse.success("가사 저장 성공", responseDto));
        } catch (LyricsException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("가사 저장에 실패했습니다."));
        }
    }
}
