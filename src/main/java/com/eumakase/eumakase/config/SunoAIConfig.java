package com.eumakase.eumakase.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * SunoAI API 연동을 위한 환경 설정
 */
@Slf4j
@Configuration
public class SunoAIConfig {
    public static final boolean DEFAULT_CUSTOM_MODE = true;
    public static final boolean DEFAULT_INSTRUMENTAL = true;
    public static final String DEFAULT_MODEL = "V4_5";
    public static final String DEFAULT_CALLBACK_URL = "https://api.example.com/callback";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpHeaders httpHeaders(SunoAIProperties props) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getSecretKey());
        return headers;
    }

    @Getter
    @Setter
    @Component
    @ConfigurationProperties(prefix = "sunoai")
    public static class SunoAIProperties {
        private String url;
        private String secretKey;
    }
}
