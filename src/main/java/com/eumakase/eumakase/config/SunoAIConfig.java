package com.eumakase.eumakase.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    public static final boolean MAKE_INSTRUMENTAL = false; //lyrics on
    public static final boolean WAIT_AUDIO = false; //no wait response
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

    @Getter
    @Setter
    @Component
    @ConfigurationProperties(prefix = "sunoai")
    public static class SunoAIProperties {
        private String url;
    }
}