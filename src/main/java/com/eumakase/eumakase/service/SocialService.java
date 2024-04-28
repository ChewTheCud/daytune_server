package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.AppleProperties;
import com.eumakase.eumakase.config.KakaoProperties;
import com.eumakase.eumakase.config.SocialConfig;
import com.eumakase.eumakase.dto.auth.apple.AppleResponseDto;
import com.eumakase.eumakase.dto.auth.kakao.KakaoResponseDto;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;


/**
 * SNS에 대한 OAuth 토큰 검증 및 API 요청 수행
 */
@Service
public class SocialService {
    private final SocialConfig socialConfig;
    private final AppleProperties appleProperties;
    private final KakaoProperties kakaoProperties;

    public SocialService(SocialConfig socialConfig, AppleProperties appleProperties, KakaoProperties kakaoProperties) {
        this.socialConfig = socialConfig;
        this.appleProperties = appleProperties;
        this.kakaoProperties = kakaoProperties;
    }

    /**
     * HTTP 요청 엔티티 생성 (Kakao)
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
                kakaoProperties.getUrl(),
                HttpMethod.GET,
                requestEntity,
                KakaoResponseDto.class);

        return responseEntity.getBody();
    }


    private String generateClientSecret() {
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(5);
        return Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, appleProperties.getKeyId())
                .setIssuer(appleProperties.getTeamId())
                .setAudience(appleProperties.getAudience())
                .setSubject(appleProperties.getClientId())
                .setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.ES256, getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(appleProperties.getPrivateKey());
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(privateKeyBytes);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            return converter.getPrivateKey(privateKeyInfo);
        } catch (Exception e) {
            throw new RuntimeException("Error converting private key from String", e);
        }
    }

    public AppleResponseDto getAppleUserProfile(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>("client_id=" + appleProperties.getClientId() +
                "&client_secret=" + generateClientSecret() +
                "&grant_type=" + appleProperties.getGrantType() +
                "&code=" + authorizationCode, headers);

        ResponseEntity<AppleResponseDto> response = socialConfig.restTemplate().exchange(
                appleProperties.getClientId() + "/auth/token", HttpMethod.POST, request, AppleResponseDto.class);

        return response.getBody();
    }
}
