package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.diary.DiaryCreateRequestDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateResponseDto;
import com.eumakase.eumakase.dto.diary.DiaryReadResponseDto;
import com.eumakase.eumakase.exception.DiaryException;
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
            return ResponseEntity.ok(ApiResponse.success("Diary 생성에 성공했습니다.",diaryCreateResponseDto));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Diary 생성에 실패했습니다."));
        }
    }

    /**
     * Diary 조회 (단일)
     */
    @GetMapping("/{diaryId}")
    public ResponseEntity<ApiResponse<DiaryReadResponseDto>> getDiary(@PathVariable Long diaryId) {
        try {
            DiaryReadResponseDto diaryReadResponseDto = diaryService.getDiary(diaryId);
            return ResponseEntity.ok(ApiResponse.success("Diary 조회에 성공했습니다.",diaryReadResponseDto));
        } catch (DiaryException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Diary 조회에 실패했습니다."));
        }
    }

    /**
     * Diary 삭제
     */
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(@PathVariable Long diaryId) {
        try {
            diaryService.deleteDiary(diaryId);
            return ResponseEntity.ok(ApiResponse.success("Diary 삭제에 성공했습니다.", null));
        } catch (DiaryException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Diary 삭제에 실패했습니다."));
        }
    }
}