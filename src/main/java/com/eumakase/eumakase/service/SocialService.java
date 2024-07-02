package com.eumakase.eumakase.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.eumakase.eumakase.config.AppleProperties;
import com.eumakase.eumakase.config.GoogleProperties;
import com.eumakase.eumakase.config.KakaoProperties;
import com.eumakase.eumakase.config.SocialConfig;
import com.eumakase.eumakase.dto.auth.apple.AppleUserInfoResponseDto;
import com.eumakase.eumakase.dto.auth.apple.AppleSocialTokenInfoResponseDto;
import com.eumakase.eumakase.dto.auth.google.GoogleUserInfoResponseDto;
import com.eumakase.eumakase.dto.auth.kakao.KakaoUserInfoResponseDto;
import com.eumakase.eumakase.exception.AuthException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.springframework.web.client.HttpClientErrorException;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

/**
 * SNS에 대한 OAuth 토큰 검증 및 API 요청 수행
 */
@Service
public class SocialService {
    private final SocialConfig socialConfig;
    private final AppleProperties appleProperties;
    private final KakaoProperties kakaoProperties;
    private final GoogleProperties googleProperties;

    public SocialService(SocialConfig socialConfig, AppleProperties appleProperties, KakaoProperties kakaoProperties, GoogleProperties googleProperties) {
        this.socialConfig = socialConfig;
        this.appleProperties = appleProperties;
        this.kakaoProperties = kakaoProperties;
        this.googleProperties = googleProperties;
    }

    /**
     * HTTP 요청 엔티티 생성
     *  @param oauthAccessToken 사용자의 OAuth 액세스 토큰
     *  @return 구성된 HTTP 요청 엔티티
     */
    public HttpEntity<String> buildHttpEntity(String oauthAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(oauthAccessToken); // Bearer 토큰으로 oauthAccessToken 추가
        return new HttpEntity<>(headers);
    }

    /**
     * Kakao 유저정보조회 API 호출 -> 응답을 KakaoUserInfoResponseDto로 반환
     * @param oauthAccessToken 사용자의 OAuth 액세스 토큰
     * @return 사용자 정보를 담고 있는 KakaoUserInfoResponseDto 객체
     */
    public KakaoUserInfoResponseDto getKakaoUserProfile(String oauthAccessToken) {
        HttpEntity<String> requestEntity = buildHttpEntity(oauthAccessToken);
        ResponseEntity<KakaoUserInfoResponseDto> responseEntity = socialConfig.restTemplate().exchange(
                kakaoProperties.getUrl(),
                HttpMethod.GET,
                requestEntity,
                KakaoUserInfoResponseDto.class);

        return responseEntity.getBody();
    }

    /**
     * Apple 토큰 인증 API 호출 -> 응답받은 ID 토큰을 JWT Decoding 처리 -> AppleUserInfoResponseDto로 반환
     * @param authorizationCode 사용자로부터 받은 인증 코드
     * @return 디코딩된 사용자 정보를 담고 있는 AppleUserInfoResponseDto 객체
     */
    public AppleUserInfoResponseDto getAppleUserProfile(String authorizationCode) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(APPLICATION_FORM_URLENCODED_VALUE));
        HttpEntity<String> request = new HttpEntity<>("client_id=" + appleProperties.getClientId() +
                "&client_secret=" + generateClientSecret() +
                "&grant_type=" + appleProperties.getGrantType() +
                "&code=" + authorizationCode, headers);

        try {
            ResponseEntity<AppleSocialTokenInfoResponseDto> response = socialConfig.restTemplate().exchange(
                    appleProperties.getAudience() + "/auth/token", HttpMethod.POST, request, AppleSocialTokenInfoResponseDto.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HttpClientErrorException(response.getStatusCode(), "Failed to retrieve Apple user profile");
            }

            DecodedJWT decodedJWT = JWT.decode(Objects.requireNonNull(response.getBody()).getIdToken());

            AppleUserInfoResponseDto appleUserInfoResponseDto = new AppleUserInfoResponseDto();
            appleUserInfoResponseDto.setSubject(decodedJWT.getClaim("sub").asString());
            appleUserInfoResponseDto.setEmail(decodedJWT.getClaim("email").asString());

            return appleUserInfoResponseDto;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new AuthException("유효하지 않거나 만료된 oauthAccessToken입니다.");
            } else {
                // General HTTP client error handling
                System.err.println("HTTP client error occurred: " + e.getStatusCode() + " " + e.getLocalizedMessage());
                throw e;
            }
        }
    }

    /**
     * Apple의 인증 서버와의 통신에 사용될 JWT을 생성하기 위해 사용되는 ClientSecret
     * ClientSecret은 토큰 요청 시 서명 목적으로 사용되며, 공개키/비공개키 인증 메커니즘이 포함됨
     * @return 생성된 JWT ClientSecret
     */
    private String generateClientSecret() {
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(5);

        return Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, appleProperties.getKeyId())
                .setIssuer(appleProperties.getTeamId())
                .setAudience(appleProperties.getAudience())
                .setSubject(appleProperties.getClientId())
                .setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
                .setIssuedAt(new Date())
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    /**
     * 애플의 JWT 클라이언트 시크릿 생성을 위한 비공개 키 로드
     * @return 로드된 RSA 비공개 키
     */
    private PrivateKey getPrivateKey() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(appleProperties.getPrivateKey());
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(privateKeyBytes);
            return converter.getPrivateKey(privateKeyInfo);
        } catch (Exception e) {
            throw new RuntimeException("Error converting private key from String", e);
        }
    }

    /**
     * Google 유저 정보 조회 API 호출 -> 응답을 GoogleUserInfoResponseDto로 반환
     * @param oauthAccessToken 사용자의 OAuth 액세스 토큰
     * @return 사용자 정보를 담고 있는 GoogleUserInfoResponseDto 객체
     */
    public GoogleUserInfoResponseDto getGoogleUserProfile(String oauthAccessToken) {
        HttpEntity<String> requestEntity = buildHttpEntity(oauthAccessToken);
        ResponseEntity<GoogleUserInfoResponseDto> responseEntity = socialConfig.restTemplate().exchange(
                googleProperties.getUrl(),
                HttpMethod.GET,
                requestEntity,
                GoogleUserInfoResponseDto.class);

        return responseEntity.getBody();
    }
}
