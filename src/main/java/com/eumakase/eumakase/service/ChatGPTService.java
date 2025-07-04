package com.eumakase.eumakase.service;

import com.eumakase.eumakase.common.constant.PromptMessages;
import com.eumakase.eumakase.config.ChatGPTConfig;
import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.PromptCategory;
import com.eumakase.eumakase.domain.PromptCategoryDetail;
import com.eumakase.eumakase.dto.chatGPT.*;
import com.eumakase.eumakase.dto.diary.EmotionInsightRequestDto;
import com.eumakase.eumakase.dto.diary.EmotionInsightResponseDto;
import com.eumakase.eumakase.exception.DiaryException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.PromptCategoryDetailRepository;
import com.eumakase.eumakase.repository.PromptCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatGPT와 연동하여 후속 질문 생성 및 감정 분석 기능을 수행하는 서비스
 */
@Slf4j
@Service
public class ChatGPTService {

    private final String model;
    private final String url;
    private final ChatGPTConfig chatGPTConfig;
    private final ObjectMapper objectMapper;
    private final DiaryRepository diaryRepository;
    private final PromptCategoryRepository promptCategoryRepository;
    private final PromptCategoryDetailRepository promptCategoryDetailRepository;

    @Value("${chatgpt.secret-key}")
    private String SECRET_KEY;

    public ChatGPTService(ChatGPTConfig chatGPTConfig,
                          ObjectMapper objectMapper,
                          DiaryRepository diaryRepository,
                          @Value("${chatgpt.model}") String model,
                          @Value("${chatgpt.url}") String url, PromptCategoryRepository promptCategoryRepository, PromptCategoryDetailRepository promptCategoryDetailRepository) {
        this.chatGPTConfig = chatGPTConfig;
        this.objectMapper = objectMapper;
        this.diaryRepository = diaryRepository;
        this.promptCategoryRepository = promptCategoryRepository;
        this.promptCategoryDetailRepository = promptCategoryDetailRepository;
        this.model = model;
        this.url = url;
    }

    /**
     * GPT 호출용 공통 HTTP 요청 구성
     */
    private HttpEntity<ChatGPTRequestDto> buildHttpEntity(ChatGPTRequestDto chatGPTRequestDto) {
        HttpHeaders headers = chatGPTConfig.httpHeaders();
        headers.setBearerAuth(SECRET_KEY);
        return new HttpEntity<>(chatGPTRequestDto, headers);
    }

    /**
     * 시스템 메시지 + 사용자 입력을 기반으로 GPT 응답 반환
     */
    private ChatGPTResponseDto callGPT(String systemMessage, String userPrompt) {
        List<Message> messages = Arrays.asList(
                new Message("system", systemMessage),
                new Message("user", userPrompt)
        );

        ChatGPTRequestDto request = new ChatGPTRequestDto(
                model, messages,
                ChatGPTConfig.TEMPERATURE,
                ChatGPTConfig.MAX_TOKEN,
                ChatGPTConfig.TOP_P,
                ChatGPTConfig.CHOICE_NUMBER,
                ChatGPTConfig.PRESENCE_PENALTY
        );

        ResponseEntity<ChatGPTResponseDto> responseEntity = chatGPTConfig.restTemplate().exchange(
                url,
                HttpMethod.POST,
                buildHttpEntity(request),
                ChatGPTResponseDto.class
        );

        return responseEntity.getBody();
    }

    /**
     * 사용자 답변 기반 후속 질문 생성
     */
    public PromptResponseDto generateFollowUpQuestion(String promptRequestDto) {
        ChatGPTResponseDto response = callGPT(PromptMessages.COUNSELOR_QUESTION, String.valueOf(promptRequestDto));
        return new PromptResponseDto(response.getChoices().get(0).getMessage().getContent());
    }

    /**
     * 질문-답변 목록 기반 감정 분석 및 공감 이유 추출
     */
    public EmotionInsightResponseDto analyzeEmotionFromQna(EmotionInsightRequestDto dto) {
        StringBuilder userPrompt = new StringBuilder();
        dto.getQuestionAnswers().forEach(qa -> {
            userPrompt.append("질문: ").append(qa.getQuestion()).append("\n");
            userPrompt.append("답변: ").append(qa.getAnswer()).append("\n\n");
        });

        // 감정 키워드 목록 조회
        String mainPrompt = String.valueOf(dto.getMainEmotion());
        PromptCategory category = promptCategoryRepository.findByMainPrompt(mainPrompt);
        if (category == null) {
            throw new DiaryException("해당 mainPrompt(" + mainPrompt + ")에 해당하는 카테고리가 없습니다.");
        }
        List<PromptCategoryDetail> details = promptCategoryDetailRepository.findByPromptCategoryId(category.getId());
        if (details.isEmpty()) {
            throw new DiaryException("해당 감정 카테고리에 상세 키워드가 없습니다.");
        }

        List<String> keywords = details.stream()
                .map(PromptCategoryDetail::getPrompt)
                .collect(Collectors.toList());
        String keywordList = String.join(", ", keywords);
        String counselorEmotionAnalysisSystemMessage = PromptMessages.generateEmotionMessage(keywordList);

        ChatGPTResponseDto response = callGPT(counselorEmotionAnalysisSystemMessage, userPrompt.toString());
        String rawContent = response.getChoices().get(0).getMessage().getContent();
        try {
            return objectMapper.readValue(rawContent, EmotionInsightResponseDto.class);
        } catch (Exception e) {
            throw new DiaryException("감정 분석 응답 파싱에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 일기 내용과 분위기를 받아 가사 생성
     */
    public String generateLyrics(String mood, String diaryContent) {
        String systemMessage = PromptMessages.LYRICS_GENERATION_SYSTEM_MESSAGE;
        String userPrompt =
                "[원하는 분위기]\n" + mood + "\n\n"
                        + "[일기 내용]\n" + diaryContent + "\n\n"
                        + "위 내용을 참고해 일기의 감정과 분위기를 잘 반영한 한국어 노래 가사를 만들어주세요.";

        ChatGPTResponseDto response = callGPT(systemMessage, userPrompt);
        return response.getChoices().get(0).getMessage().getContent();
    }


    /**
     * 일기 요약이 비어있을 경우 GPT를 통해 요약 생성
     */
    @Transactional
    public void updateDiarySummary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary ID가 " + diaryId + "인 데이터를 찾을 수 없습니다."));

        if (diary.getSummary() == null || diary.getSummary().isEmpty()) {
            PromptResponseDto response = generateFollowUpQuestion(diary.getContent());
            diary.setSummary(response.getContent());
            diaryRepository.save(diary);
        }
    }
}
