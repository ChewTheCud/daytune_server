package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.lyrics.LyricsCreateRequestDto;
import com.eumakase.eumakase.dto.lyrics.LyricsCreateResponseDto;
import com.eumakase.eumakase.dto.lyrics.LyricsReadResponseDto;
import com.eumakase.eumakase.service.LyricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v2/lyrics")
@RequiredArgsConstructor
public class LyricsController {
    private final LyricsService lyricsService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<LyricsCreateResponseDto>> generateLyrics(
            @Valid @RequestBody LyricsCreateRequestDto requestDto) {
        LyricsCreateResponseDto responseDto = lyricsService.generateLyrics(requestDto);
        return ResponseEntity.ok(ApiResponse.success("가사 생성 성공", responseDto));
    }

    @GetMapping("/{lyricsId}")
    public ResponseEntity<ApiResponse<LyricsReadResponseDto>> getLyrics(
            @PathVariable Long lyricsId) {
        LyricsReadResponseDto responseDto = lyricsService.readLyrics(lyricsId);
        return ResponseEntity.ok(ApiResponse.success("가사 조회 성공", responseDto));
    }
}
