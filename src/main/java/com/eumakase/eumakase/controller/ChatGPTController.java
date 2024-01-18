package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.chatGPT.PromptRequestDto;
import com.eumakase.eumakase.dto.chatGPT.PromptResponseDto;
import com.eumakase.eumakase.service.ChatGPTService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * ChatGPT API
 */
@RestController
@RequestMapping(value = "/api/v1/chatGpt")
public class ChatGPTController {
    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    /**
     * ChatGPT 프롬프트 입력하여 문장 단어 추출
     */
    @PostMapping("/question")
    public ResponseEntity<ApiResponse<PromptResponseDto>> sendPrompt(@RequestBody PromptRequestDto promptRequestDto) {
        try {
            PromptResponseDto promptResponse = chatGPTService.sendPrompt(promptRequestDto);
            return ResponseEntity.ok(ApiResponse.success("생성에 성공했습니다.",promptResponse));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("생성 실패했습니다."));
        }
    }
}
