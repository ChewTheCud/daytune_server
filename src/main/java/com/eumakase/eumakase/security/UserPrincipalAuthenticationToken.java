package com.eumakase.eumakase.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;


/**
 * 사용자의 UserPrincipal을 기반으로 한 Spring Security의 인증 토큰
 */
public class UserPrincipalAuthenticationToken extends AbstractAuthenticationToken {
    private final UserPrincipal principal; // 사용자의 주요 정보를 담고 있는 UserPrincipal 객체

    // 사용자의 인증 정보를 포함
    public UserPrincipalAuthenticationToken(UserPrincipal principal) {
        super(principal.getAuthorities()); // 부모 클래스에 사용자 권한 목록을 전달
        this.principal = principal;
        setAuthenticated(true); // 이 토큰을 인증된 것으로 설정
    }

    // 인증된 사용자의 자격 증명을 반환
    // 이 경우 자격 증명(비밀번호, 토큰 등) 정보는 사용하지 않으므로 null 반환
    @Override
    public Object getCredentials() {
        return null;
    }

    // 인증된 사용자의 주요 정보를 반환
    @Override
    public UserPrincipal getPrincipal() {
        return principal;
    }
}