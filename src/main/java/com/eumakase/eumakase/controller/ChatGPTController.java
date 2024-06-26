package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.chatGPT.PromptRequestDto;
import com.eumakase.eumakase.dto.chatGPT.PromptResponseDto;
import com.eumakase.eumakase.service.ChatGPTService;
import com.eumakase.eumakase.util.enums.PromptType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


/**
 * ChatGPT API
 */
@RestController
@RequestMapping(value = "/api/v1/chatgpt")
public class ChatGPTController {
    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    /**
     * ChatGPT 프롬프트 입력하여 문장 단어 추출
     */
    @PostMapping("/question")
    public ResponseEntity<ApiResponse<PromptResponseDto>> sendPrompt(@Valid @RequestBody PromptRequestDto promptRequestDto) {
        try {
            PromptResponseDto promptResponse = chatGPTService.sendPrompt(promptRequestDto, PromptType.CONTENT_EMOTION_ANALYSIS);
            return ResponseEntity.ok(ApiResponse.success("단어 추출에 성공했습니다.",promptResponse));
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("단어 추출에 실패했습니다. 상세 정보: " + e.getMessage()));
        }
    }
}
