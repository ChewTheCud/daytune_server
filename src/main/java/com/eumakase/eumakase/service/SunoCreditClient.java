package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.SunoAIConfig;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Suno 잔여 크레딧 조회 (GET /api/v1/generate/credit). 미터링 비용 실측용이다.
 * - 본 기능을 막지 않는다: 타임아웃 3초, 실패·비정상 응답은 null을 반환하고 삼킨다
 * - 이 조회 자체는 미터링에 기록하지 않는다
 * - 로그에는 예외 클래스명만 남긴다 (응답 본문·키 노출 방지)
 */
@Slf4j
@Component
public class SunoCreditClient {

    static final int TIMEOUT_MILLIS = 3_000;
    private static final String CREDIT_PATH = "/api/v1/generate/credit";

    private final RestTemplate restTemplate;
    private final SunoAIConfig.SunoAIProperties properties;
    private final String secretKey;

    @Autowired
    public SunoCreditClient(SunoAIConfig.SunoAIProperties properties, @Value("${sunoai.secret-key}") String secretKey) {
        this(timeoutRestTemplate(), properties, secretKey);
    }

    SunoCreditClient(RestTemplate restTemplate, SunoAIConfig.SunoAIProperties properties, String secretKey) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.secretKey = secretKey;
    }

    /**
     * @return 잔여 크레딧. 조회 실패·비정상 응답이면 null
     */
    public Long fetchBalance() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(secretKey);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    properties.getUrl() + CREDIT_PATH, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            JsonNode body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null
                    || body.path("code").asInt() != 200 || !body.path("data").isIntegralNumber()) {
                log.warn("[미터링] Suno 크레딧 조회 응답이 비정상입니다.");
                return null;
            }
            return body.path("data").asLong();
        } catch (Exception e) {
            log.warn("[미터링] Suno 크레딧 조회에 실패했습니다: {}", e.getClass().getSimpleName());
            return null;
        }
    }

    private static RestTemplate timeoutRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MILLIS);
        factory.setReadTimeout(TIMEOUT_MILLIS);
        return new RestTemplate(factory);
    }
}
