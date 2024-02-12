package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.diary.DiaryCreateRequestDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateResponseDto;
import com.eumakase.eumakase.dto.diary.DiaryReadResponseDto;
import com.eumakase.eumakase.exception.DiaryException;
import com.eumakase.eumakase.service.DiaryService;
import com.eumakase.eumakase.service.MusicService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<Void>> updateMusicUrls() {
        System.out.println("test");
        try {
            musicService.updateMusicFileUrls();
            return ResponseEntity.ok(ApiResponse.success("Music 데이터 FileUrl 추가에 성공했습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Music 데이터 FileUrl 추가에 실패했습니다."));
        }
    }
}