package com.eumakase.eumakase.service;

import com.eumakase.eumakase.common.constant.PromptMessages;
import com.eumakase.eumakase.config.ChatGPTConfig;
import com.eumakase.eumakase.dto.chatGPT.*;
import com.eumakase.eumakase.util.enums.PromptType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ChatGPTService {

    private final String model;
    private final String url;
    private final ChatGPTConfig chatGPTConfig;

    @Value("${chatgpt.secret-key}")
    private String SECRET_KEY;

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
        headers.setBearerAuth(SECRET_KEY);
        return new HttpEntity<>(chatGPTRequestDto, headers);
    }

    /**
     * ChatGPT API에 요청 -> 응답을 ChatGPTResponseDto로 반환
     */
    public ChatGPTResponseDto getResponse(HttpEntity<ChatGPTRequestDto> requestEntity) {
        System.out.println("requestEntity: "+requestEntity);
        ResponseEntity<ChatGPTResponseDto> responseEntity = chatGPTConfig.restTemplate().exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ChatGPTResponseDto.class);
System.out.println("responseEntity: "+responseEntity);
        return responseEntity.getBody();
    }

    /**
     * ChatGPT 프롬프트 답변 생성
     */
    public PromptResponseDto sendPrompt(PromptRequestDto promptRequestDto, PromptType promptType) {
        try {
            String systemMessage = "";
            if (promptType == PromptType.CONTENT_EMOTION_ANALYSIS) {
                systemMessage = PromptMessages.CONTENT_EMOTION_ANALYSIS;
            } else if (promptType == PromptType.COUNSELOR_CONCEPT) {
                systemMessage = PromptMessages.COUNSELOR_CONCEPT;
            } else {
                log.error("Invalid prompt type provided: {}", promptType);
                throw new IllegalArgumentException("Invalid prompt type");
            }

            // 메시지 리스트를 생성
            List<Message> messages = Arrays.asList(
                    //new Message("system", "Analyze the contents of the diary and guess the emotions, and answer in Korean only with various words including non-overlapping adjectives"),
                    new Message("system", systemMessage),
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
            System.out.println("chatGPTRequestDto: "+ chatGPTRequestDto);
            // [STEP2] API 응답을 받음
            ChatGPTResponseDto chatGPTResponseDto =  this.getResponse(this.buildHttpEntity(chatGPTRequestDto));
            System.out.println("chatGPTResponseDto: "+ chatGPTResponseDto);

            if (chatGPTResponseDto == null || chatGPTResponseDto.getChoices().isEmpty()) {
                log.error("No response or choices received from ChatGPT API");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get response from ChatGPT");
            }

            // [STEP3] 첫 번째 선택지의 메시지 내용을 반환
            Choice firstChoice = chatGPTResponseDto.getChoices().get(0);
            System.out.println("firstChoice: "+ firstChoice);
            return new PromptResponseDto(firstChoice.getMessage().getContent());
        } catch (IllegalArgumentException | ResponseStatusException e) {
            log.error("Error occurred during prompt processing: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", e);
        }
    }

}
