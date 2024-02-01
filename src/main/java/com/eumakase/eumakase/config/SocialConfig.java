package com.eumakase.eumakase.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * 소셜로그인 연동을 위한 환경 설정
 */
@Slf4j
@Configuration
public class SocialConfig {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String MEDIA_TYPE = "application/json; charset=UTF-8";

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }

    @Bean
    public HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(MEDIA_TYPE));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
