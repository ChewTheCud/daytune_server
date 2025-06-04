package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.dto.diary.DiaryCreateRequestDto;
import com.eumakase.eumakase.dto.diary.DiaryCreateResponseDto;
import com.eumakase.eumakase.dto.diary.DiaryReadResponseDto;
import com.eumakase.eumakase.dto.music.MusicReadResponseDto;
import com.eumakase.eumakase.exception.DiaryException;
import com.eumakase.eumakase.exception.MusicException;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.security.UserPrincipal;
import com.eumakase.eumakase.service.DiaryService;
import com.eumakase.eumakase.service.MusicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Diary API
 */
@RestController
@RequestMapping(value = "/api/v1/diaries")
public class DiaryController {
    private final DiaryService diaryService;
    private final MusicService musicService;

    public DiaryController(DiaryService diaryService, MusicService musicService) {
        this.diaryService = diaryService;
        this.musicService = musicService;
    }

    /**
     * Diary 생성
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<DiaryCreateResponseDto>> createDiary(@Valid @RequestBody DiaryCreateRequestDto diaryCreateRequestDto,
                                                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        Long authenticatedUserId = currentUser.getId();
        try {
            DiaryCreateResponseDto diaryCreateResponseDto = diaryService.createDiary(authenticatedUserId, diaryCreateRequestDto);

            return ResponseEntity.ok(ApiResponse.success("Diary 생성에 성공했습니다.", diaryCreateResponseDto));
        } catch (UserException e) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(e.getMessage()));
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
     * 특정 유저의 Diary 조회 (전체)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<DiaryReadResponseDto>>> getAllDiariesByUserId(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Long authenticatedUserId = currentUser.getId();
            List<DiaryReadResponseDto> response = diaryService.getAllDiariesByUserId(authenticatedUserId);
            return ResponseEntity.ok(ApiResponse.success("사용자의 모든 Diary 조회에 성공했습니다", response));
        } catch (DiaryException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
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

    /**
     * 특정 일기의 음악 정보 조회
     * @param diaryId 일기의 ID
     * @return 음악 정보 리스트
     */
    @GetMapping("/{diaryId}/musics")
    public ResponseEntity<ApiResponse<List<MusicReadResponseDto>>> getMusicByDiaryId(@PathVariable Long diaryId) {
        try {
            List<MusicReadResponseDto> musicInfoList = musicService.getMusicByDiaryId(diaryId);
            return ResponseEntity.ok(ApiResponse.success("음악 정보 조회에 성공했습니다.", musicInfoList));
        } catch (MusicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("음악 정보 조회에 실패했습니다."));
        }
    }
}