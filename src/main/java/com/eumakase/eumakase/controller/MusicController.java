package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.music.MusicCreateRequestDto;
import com.eumakase.eumakase.dto.music.MusicSelectionRequestDto;
import com.eumakase.eumakase.dto.music.MusicSelectionResponseDto;
import com.eumakase.eumakase.dto.music.MusicUpdateFileUrlsResultDto;
import com.eumakase.eumakase.dto.sunoAI.SunoAIGenerationResultDto;
import com.eumakase.eumakase.exception.MusicException;
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
@RequestMapping(value = "/api/v2/music")
public class MusicController {
    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    /**
     * 음악 생성
     * @param musicCreateRequestDto 음악 생성을 위한 입력값
     * @return 생성된 음악 ID
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SunoAIGenerationResultDto>> generateMusic(
            @RequestBody MusicCreateRequestDto musicCreateRequestDto) {
        try {
            musicService.createMusic(musicCreateRequestDto);
            return ResponseEntity.ok(ApiResponse.success("음악 생성 요청 완료했습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("음악 생성 요청 실패했습니다: " + e.getMessage()));
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
            // 음악 생성 서비스 호출
            List<Map<String, String>> audioUrl = musicService.getSunoAIMusicDetails(sunoAiMusicId);
            return ResponseEntity.ok(ApiResponse.success("음악 상세 정보 조회 성공", audioUrl).getData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((List<Map<String, String>>) ApiResponse.error("음악 상세 정보 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 음악 파일 URL을 업데이트
     * @return 업데이트 결과를 포함한 응답
     */
    @GetMapping("/urls")
    public ResponseEntity<ApiResponse<MusicUpdateFileUrlsResultDto>> updateMusicUrls() {
        try {
            // 음악 파일 URL 업데이트 서비스 호출
            MusicUpdateFileUrlsResultDto result = musicService.updateMusicFileUrls();
            String message = String.format("총 %d개의 음악 데이터 중, %d개 데이터가 성공적으로 파일 URL이 업데이트되었습니다.",
                    result.getUpdatedMusicFiles().size() + result.getNotUpdatedMusicFiles().size(), result.getUpdatedMusicFiles().size());

            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Music 데이터 FileUrl 추가에 실패했습니다."));
        }
    }

    /**
     * 특정 일기의 음악 중 하나를 선택하고 나머지 음악을 삭제
     * @param requestDto 선택할 음악과 일기의 정보를 담은 DTO
     * @return 성공 또는 실패 응답
     */
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<MusicSelectionResponseDto>> selectMusic(@RequestBody MusicSelectionRequestDto requestDto) {
        try {
            // 음악 선택 서비스 호출
            MusicSelectionResponseDto responseDto = musicService.selectMusic(requestDto.getDiaryId(), requestDto.getMusicId());
            return ResponseEntity.ok(ApiResponse.success("음악 선택에 성공했습니다.", responseDto));
        } catch (MusicException e) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}