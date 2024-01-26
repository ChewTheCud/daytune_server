package com.eumakase.eumakase.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 사용자 인증 정보 및 권한 관리
 */
@Getter
@Builder
public class UserPrincipal implements UserDetails {
    private final Long userId; // 사용자의 고유 ID
    private final String email; // 사용자의 이메일 주소
    @JsonIgnore
    private final String password; // 사용자의 비밀번호, JSON 직렬화 시 무시됨
    private final Collection<? extends GrantedAuthority> authorities; // 사용자에게 부여된 권한 목록

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // 사용자에게 부여된 권한 반환
    }

    @Override
    public String getPassword() {
        return password; // 사용자 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return email; // 사용자 이름으로 이메일 사용
    }

    // 계정 만료 여부 반환 (true: 만료되지 않음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부 반환 (true: 잠겨있지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 자격 증명 만료 여부 반환 (true: 만료되지 않음)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 반환 (true: 활성화되어 있음)
    @Override
    public boolean isEnabled() {
        return true;
    }
}