package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.SocialConfig;
import com.eumakase.eumakase.dto.auth.kakao.KakaoResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

/**
 * SNS에 대한 OAuth 토큰 검증 및 API 요청 수행
 */
@Service
public class SocialService {

    private final String url;
    private final SocialConfig socialConfig;

    public SocialService(SocialConfig socialConfig, @Value("${kakao.url}") String url) {
        this.socialConfig = socialConfig;
        this.url = url;
    }

    /**
     * HTTP 요청 엔티티 생성
     */
    public HttpEntity<String> buildHttpEntity(String oauthAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(oauthAccessToken); // Bearer 토큰으로 oauthAccessToken 추가
        return new HttpEntity<>(headers);
    }

    /**
     * Kakao 유저정보조회 API에 요청 -> 응답을 KakaoResponseDto로 반환
     */
    public KakaoResponseDto getKakaoUserProfile(String oauthAccessToken) {
        HttpEntity<String> requestEntity = buildHttpEntity(oauthAccessToken);
        ResponseEntity<KakaoResponseDto> responseEntity = socialConfig.restTemplate().exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                KakaoResponseDto.class);

        return responseEntity.getBody();
    }
}
