package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.ChatGPTConfig;
import com.eumakase.eumakase.dto.chatGPT.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ChatGPTService {

    private final String model;
    private final String url;
    private final ChatGPTConfig chatGPTConfig;

    public ChatGPTService(ChatGPTConfig chatGPTConfig, @Value("${chatgpt.model}") String model, @Value("${chatgpt.url}") String url) {
        this.chatGPTConfig = chatGPTConfig;
        this.model = model;
        this.url = url;
    }

    /**
     * HTTP 요청 엔티티 생성
     */
    public HttpEntity<ChatGPTRequestDto> buildHttpEntity(ChatGPTRequestDto chatGPTRequestDto) {
        HttpHeaders headers = chatGPTConfig.httpHeaders();

        return new HttpEntity<>(chatGPTRequestDto, headers);
    }

    /**
     * ChatGPT API에 요청 -> 응답을 ChatGPTResponseDto로 반환
     */
    public ChatGPTResponseDto getResponse(HttpEntity<ChatGPTRequestDto> requestEntity) {
        ResponseEntity<ChatGPTResponseDto> responseEntity = chatGPTConfig.restTemplate().exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ChatGPTResponseDto.class);

        return responseEntity.getBody();
    }

    /**
     * ChatGPT 프롬프트 답변 생성
     */
    public PromptResponseDto sendPrompt(PromptRequestDto promptRequestDto) {
        // 메시지 리스트를 생성
        List<Message> messages = Arrays.asList(
                new Message("system", "Analyze the contents of the diary and guess the emotions, and answer in Korean only with various words including non-overlapping adjectives"),
                //new Message("system", "You are a counselor who can give healing to users who have kept a diary for today. Please empathize with the user in two sentences."),
                new Message("user", promptRequestDto.getPrompt())
        );

        log.debug("[+] 프롬프트를 수행합니다.");

        // [STEP1] ChatGPTRequestDto 객체 생성
        ChatGPTRequestDto chatGPTRequestDto = new ChatGPTRequestDto(
                model,
                messages,
                ChatGPTConfig.TEMPERATURE,
                ChatGPTConfig.MAX_TOKEN,
                ChatGPTConfig.TOP_P,
                ChatGPTConfig.CHOICE_NUMBER
        );

        // [STEP2] API 응답을 받음
        ChatGPTResponseDto chatGPTResponseDto =  this.getResponse(this.buildHttpEntity(chatGPTRequestDto));

        // [STEP3] 첫 번째 선택지의 메시지 내용을 반환
        Choice firstChoice = chatGPTResponseDto.getChoices().get(0);
        return new PromptResponseDto(firstChoice.getMessage().getContent());
    }
}
