package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.AppleProperties;
import com.eumakase.eumakase.config.KakaoProperties;
import com.eumakase.eumakase.config.SocialConfig;
import com.eumakase.eumakase.dto.auth.apple.AppleResponseDto;
import com.eumakase.eumakase.dto.auth.kakao.KakaoResponseDto;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    public AppleResponseDto getAppleUserProfile(String authorizationCode) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        System.out.println("test generateClientSecret():"+generateClientSecret());
        HttpEntity<String> request = new HttpEntity<>("client_id=" + appleProperties.getClientId() +
                "&client_secret=" + generateClientSecret() +
                "&grant_type=" + appleProperties.getGrantType() +
                "&code=" + authorizationCode, headers);

        System.out.println("request body:" + request.getBody());

        ResponseEntity<AppleResponseDto> response = socialConfig.restTemplate().exchange(
                appleProperties.getClientId() + "/auth/token", HttpMethod.POST, request, AppleResponseDto.class);

        System.out.println("test response:" + response);
        return response.getBody();
    }

    private String generateClientSecret() throws IOException {
        LocalDateTime expiration = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5);
        return Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, appleProperties.getKeyId())
                .setIssuer(appleProperties.getTeamId())
                .setAudience(appleProperties.getAudience())
                .setSubject(appleProperties.getClientId())
                .setExpiration(Date.from(expiration.toInstant(ZoneOffset.UTC)))
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(SignatureAlgorithm.ES256, getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() throws IOException {
        ASN1InputStream asn1InputStream = null;
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(appleProperties.getPrivateKey());
            asn1InputStream = new ASN1InputStream(privateKeyBytes);
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(asn1InputStream.readObject());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            return converter.getPrivateKey(privateKeyInfo);
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalArgumentException("Failed to decode or convert private key", e);
        } finally {
            asn1InputStream.close();
        }
    }
}
