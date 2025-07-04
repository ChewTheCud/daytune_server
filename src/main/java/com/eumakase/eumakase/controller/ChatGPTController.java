package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.chatGPT.PromptRequestDto;
import com.eumakase.eumakase.dto.chatGPT.PromptResponseDto;
import com.eumakase.eumakase.dto.diary.EmotionInsightRequestDto;
import com.eumakase.eumakase.dto.diary.EmotionInsightResponseDto;
import com.eumakase.eumakase.service.ChatGPTService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * ChatGPT API 컨트롤러
 */
@RestController
@RequestMapping("/api/v2/chatgpt")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    /**
     * 사용자 응답 기반 후속 질문 생성
     */
    @PostMapping("/question")
    public ResponseEntity<ApiResponse<PromptResponseDto>> generateFollowUpQuestion(@Valid @RequestBody PromptRequestDto promptRequestDto) {
        try {
            PromptResponseDto response = chatGPTService.generateFollowUpQuestion(String.valueOf(promptRequestDto));
            return ResponseEntity.ok(ApiResponse.success("후속 질문 생성에 성공했습니다.", response));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(ApiResponse.error(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("후속 질문 생성에 실패했습니다. 상세 정보: " + e.getMessage()));
        }
    }

    /**
     * 질문-답변 기반 감정 분석
     */
    @PostMapping("/emotion")
    public ResponseEntity<ApiResponse<EmotionInsightResponseDto>> analyzeEmotion(@Valid @RequestBody EmotionInsightRequestDto requestDto) {
        try {
            EmotionInsightResponseDto response = chatGPTService.analyzeEmotionFromQna(requestDto);
            return ResponseEntity.ok(ApiResponse.success("감정 분석에 성공했습니다.", response));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(ApiResponse.error(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("감정 분석에 실패했습니다. 상세 정보: " + e.getMessage()));
        }
    }
}
