package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.music.MusicUpdateFileUrlsResultDto;
import com.eumakase.eumakase.service.MusicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * 음악 파일 URL 업데이트
     */
    //
    @GetMapping("/urls")
    public ResponseEntity<ApiResponse<String>> updateMusicUrls() {
        try {
            MusicUpdateFileUrlsResultDto result = musicService.updateMusicFileUrls();

            String message = String.format("총 %d개의 미완성 음악 데이터 중, %d개 데이터에 성공적으로 파일 URL을 할당했습니다.",
                    result.getTotalNullUrls(), result.getUpdatedUrlsCount());

            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Music 데이터 FileUrl 추가에 실패했습니다."));
        }
    }
}