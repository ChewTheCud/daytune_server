package com.eumakase.eumakase.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JWT 토큰에서 사용자 관련 정보 및 권한을 추출하여 UserPrincipal 객체로 변환
 */
@Component
public class JwtToPrincipalConverter {

    // DecodedJWT에서 UserPrincipal로 변환.
    public UserPrincipal convert(DecodedJWT jwt) {
        return UserPrincipal.builder()
                .userId(Long.valueOf(jwt.getSubject())) // JWT 'sub' (subject) 클레임에서 사용자 ID 추출
                .email(jwt.getClaim("e").asString()) // 'e' 커스텀 클레임에서 이메일 추출
                .authorities(extractAuthoritiesFromClaim(jwt)) // 권한 정보 추출
                .build();
    }

    // JWT의 클레임에서 권한을 추출
    private List<SimpleGrantedAuthority> extractAuthoritiesFromClaim(DecodedJWT jwt) {
        var claim = jwt.getClaim("a"); // 'a' 커스텀 클레임에서 권한 정보 추출
        if (claim.isNull() || claim.isMissing()) return List.of(); // 클레임이 없거나 누락된 경우 빈 목록 반환
        return claim.asList(SimpleGrantedAuthority.class); // SimpleGrantedAuthority 객체의 목록으로 변환
    }
}
