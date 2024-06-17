package com.eumakase.eumakase.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * JWT 발행 클래스
 * JWT 토큰 생성, 토큰에 사용자의 식별 정보와 역할 정보를 포함, 설정된 비밀 키를 사용하여 토큰을 안전하게 서명하는 역할
 */
@Component // Spring의 컴포넌트로 등록하여 의존성 주입 및 관리 가능하게 함.
@RequiredArgsConstructor
public class JwtIssuer {
    private final JwtProperties properties; // JWT 설정을 담고 있는 properties 객체.

    /**
     * JWT를 발행하는 메서드.
     *
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param roles 사용자의 역할 목록
     * @return 서명된 JWT 문자열
     */
    public String issue(long userId, String email, List<String> roles) {
        return JWT.create()
                .withSubject(String.valueOf(userId)) // JWT 'sub' (subject) 클레임을 설정 (사용자 ID).
                .withExpiresAt(Instant.now().plus(Duration.of(7, ChronoUnit.DAYS))) // JWT 만료 시간을 현재로부터 1일 후로 설정.
                .withClaim("e", email) // 사용자 이메일을 커스텀 클레임으로 추가.
                .withClaim("a", roles) // 사용자 역할을 커스텀 클레임으로 추가.
                .sign(Algorithm.HMAC256(properties.getSecretKey())); // HMAC256 알고리즘과 properties 객체의 비밀 키를 사용하여 JWT 서명.
    }

    /**
     * 리프레시 토큰을 발행하는 메서드.
     *
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @return 서명된 리프레시 토큰 문자열
     */
    public String issueRefreshToken(long userId, String email) {
        return JWT.create()
                .withSubject(String.valueOf(userId)) // 사용자 ID를 주제로 설정
                .withExpiresAt(Instant.now().plus(Duration.of(7, ChronoUnit.DAYS))) // 만료 시간을 현재로부터 7일 후로 설정
                .withClaim("e", email) // 사용자 이메일을 클레임으로 추가
                .sign(Algorithm.HMAC256(properties.getSecretKey())); // HMAC256 알고리즘을 사용하여 서명
    }

    //리프레시 토큰의 유효성 검증
    public boolean validateRefreshToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(properties.getSecretKey()))
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}