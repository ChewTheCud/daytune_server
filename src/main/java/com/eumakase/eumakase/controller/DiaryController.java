package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.diary.DiaryCreateRequestDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateResponseDto;
import com.eumakase.eumakase.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Diary API
 */
@RestController
@RequestMapping(value = "/api/v1/diary")
public class DiaryController {
    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /**
     * Diary 생성
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<DiaryCreateResponseDto>> createDiary(@Valid @RequestBody DiaryCreateRequestDto diaryCreateRequestDto) {
        try {
            DiaryCreateResponseDto diaryCreateResponseDto = diaryService.createDiary(diaryCreateRequestDto);
            return ResponseEntity.ok(ApiResponse.success(diaryCreateResponseDto));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Diary 생성에 실패했습니다."));
        }
    }
}