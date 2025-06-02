package com.eumakase.eumakase.config;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * ChatGPT API 연동을 위한 환경 설정
 */
@Slf4j
@Configuration
public class ChatGPTConfig {
    public static final Integer MAX_TOKEN = 300;
    public static final Double TEMPERATURE = 1.0; //사용할 샘플링 온도 0~2. 값이 높을수록 무작위 출력, 값이 낮을수록 집중적&결정적
    public static final Double TOP_P = 1.0;
    public static final Integer CHOICE_NUMBER = 1;
    public static final Double PRESENCE_PENALTY = 0.6; //같은 단어 반복 방지, 창의성 증가

    @Value("${chatgpt.secret-key}")
    private String SECRET_KEY;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
