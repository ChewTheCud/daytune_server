package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.music.MusicUpdateFileUrlsResultDto;
import com.eumakase.eumakase.dto.sunoAI.SunoAIGenerationResultDto;
import com.eumakase.eumakase.dto.sunoAI.SunoAIRequestDto;
import com.eumakase.eumakase.service.MusicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Music API
 */
@RestController
@RequestMapping(value = "/api/v1/music")
public class MusicController {
    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    /**
     * 음악 생성
     * @param sunoAIRequestDto 음악 생성을 위한 설명
     * @return 생성된 음악 ID
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SunoAIGenerationResultDto>> generateMusic(@RequestBody SunoAIRequestDto sunoAIRequestDto) {
        try {
            List<String> songIds = musicService.generateSunoAIMusic(sunoAIRequestDto);
            SunoAIGenerationResultDto result = new SunoAIGenerationResultDto(songIds);
            return ResponseEntity.ok(ApiResponse.success("음악 생성 완료", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("음악 생성 실패: " + e.getMessage()));
        }
    }

    /**
     * 음악 상세 정보 조회
     * @param sunoAiMusicId 음악 ID
     * @return 음악의 상세 정보
     */
    @GetMapping("/sunoai/{sunoAiMusicId}")
    public ResponseEntity<List<Map<String, String>>> getMusicDetails(@PathVariable String sunoAiMusicId) {
        try {
            List<Map<String, String>> audioUrl = musicService.getSunoAIMusicDetails(sunoAiMusicId);
            return ResponseEntity.ok(ApiResponse.success("음악 상세 정보 조회 성공", audioUrl).getData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((List<Map<String, String>>) ApiResponse.error("음악 상세 정보 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 음악 파일 URL 업데이트
     */
    //
    @GetMapping("/urls")
    public ResponseEntity<ApiResponse<MusicUpdateFileUrlsResultDto>> updateMusicUrls() {
        try {
            MusicUpdateFileUrlsResultDto result = musicService.updateMusicFileUrls();
            String message = String.format("총 %d개의 음악 데이터 중, %d개 데이터가 성공적으로 파일 URL이 업데이트되었습니다.",
                    result.getUpdatedMusicFiles().size() + result.getNotUpdatedMusicFiles().size(), result.getUpdatedMusicFiles().size());

            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Music 데이터 FileUrl 추가에 실패했습니다."));
        }
    }
}