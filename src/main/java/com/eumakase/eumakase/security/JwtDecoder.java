package com.eumakase.eumakase.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * JWT 토큰을 디코딩하는 역할을 수행하며, 시크릿 키를 사용하여 토큰이 유효한지 확인
 */
@Component
@RequiredArgsConstructor
public class JwtDecoder {
    private final JwtProperties properties; // JWT 설정을 담고 있는 properties 객체.

    public DecodedJWT decode(String token) {
        // JWT 검증기를 구성하고, 제공된 토큰을 검증 및 디코드
        return JWT.require(Algorithm.HMAC256(properties.getSecretKey())) // HMAC256 알고리즘과 properties 객체의 비밀 키를 사용하여 알고리즘 설정.
                .build() // JWT 검증기 인스턴스 생성
                .verify(token); // 제공된 JWT 토큰 검증 및 디코드
    }
}