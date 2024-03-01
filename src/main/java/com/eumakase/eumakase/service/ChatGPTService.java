package com.eumakase.eumakase.service;

import com.eumakase.eumakase.common.constant.PromptMessages;
import com.eumakase.eumakase.config.ChatGPTConfig;
import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.dto.chatGPT.*;
import com.eumakase.eumakase.exception.DiaryException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.util.enums.PromptType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ChatGPTService {
    PromptMessages promptMessages;
    private final String model;
    private final String url;
    private final ChatGPTConfig chatGPTConfig;

    @Value("${chatgpt.secret-key}")
    private String SECRET_KEY;

    @Autowired
    private DiaryRepository diaryRepository;

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
        String systemMessage = "";
        if (promptType == PromptType.CONTENT_EMOTION_ANALYSIS) {
            systemMessage= PromptMessages.CONTENT_EMOTION_ANALYSIS;
        }
        if (promptType == PromptType.COUNSELOR_CONCEPT) {
            systemMessage= PromptMessages.COUNSELOR_CONCEPT;
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
        // [STEP3] 첫 번째 선택지의 메시지 내용을 반환
        Choice firstChoice = chatGPTResponseDto.getChoices().get(0);
        System.out.println("firstChoice: "+ firstChoice);
        return new PromptResponseDto(firstChoice.getMessage().getContent());
    }

    /**
     * Diary 내 summary 업데이트 (GPT 연동)
     */
    // diaryService내 updateDiarySummary 구현시 순환참조 문제로 chatGPTService에 구현
    @Transactional
    public void updateDiarySummary(Long diaryId) {
        // Diary를 찾고 없으면 예외 발생
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary ID가 " + diaryId + "인 데이터를 찾을 수 없습니다."));

        // summary가 비어있는 경우에만 요약 작업 수행
        if (diary.getSummary() == null || diary.getSummary().isEmpty()) {
            PromptRequestDto promptRequestDto = new PromptRequestDto(diary.getContent());
            PromptResponseDto promptResponseDto = sendPrompt(promptRequestDto, PromptType.COUNSELOR_CONCEPT);

            diary.setSummary(promptResponseDto.getContent());
            diaryRepository.save(diary);
        }
    }
}
