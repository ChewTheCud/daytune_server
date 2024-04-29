package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.AppleProperties;
import com.eumakase.eumakase.config.KakaoProperties;
import com.eumakase.eumakase.config.SocialConfig;
import com.eumakase.eumakase.dto.auth.apple.AppleResponseDto;
import com.eumakase.eumakase.dto.auth.kakao.KakaoResponseDto;
import io.jsonwebtoken.JwtException;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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

    public String generateClientSecret() {
        try {
            LocalDateTime expiration = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5);
            PrivateKey privateKey = getPrivateKey(); // 예외 발생 가능한 부분
            System.out.println("privateKey:"+privateKey.toString());
            return Jwts.builder()
                    .setHeaderParam(JwsHeader.KEY_ID, appleProperties.getKeyId())
                    .setIssuer(appleProperties.getTeamId())
                    .setAudience(appleProperties.getAudience())
                    .setSubject(appleProperties.getClientId())
                    .setExpiration(Date.from(expiration.toInstant(ZoneOffset.UTC)))
                    .setIssuedAt(Date.from(Instant.now()))
                    .signWith(SignatureAlgorithm.ES256, privateKey)
                    .compact();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error retrieving private key: " + e.getMessage(), e);
        } catch (JwtException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error creating JWT: " + e.getMessage(), e);
        } catch (Exception e) {
            // 포괄적 예외 처리
            System.out.println(e.getMessage());
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage(), e);
        }
    }

    private PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyPEM = appleProperties.getPrivateKey();
        System.out.println("privateKeyPEM key: "+ privateKeyPEM);
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("EC"); // ECDSA 키를 위한 인스턴스, RSA 등 다른 키 타입인 경우 변경 필요
        System.out.println("privateKeyPEM key: "+ keyFactory.generatePrivate(keySpec));
        return keyFactory.generatePrivate(keySpec);
    }
}
