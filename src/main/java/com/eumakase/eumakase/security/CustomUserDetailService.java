package com.eumakase.eumakase.security;

import com.eumakase.eumakase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring Security의 UserDetailsService 인터페이스를 구현한 사용자 정의 서비스
 * 사용자의 정보를 가져와 Spring Security가 이를 활용하여 사용자를 인증하는 데 사용
 */
@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserService userService;
    @Override
    public UserDetails loadUserByUsername(String sndId) throws UsernameNotFoundException {
        var user = userService.findBySnsId(sndId).orElseThrow();

        //Spring Security의 UserDetails 인터페이스를 구현한 사용자의 세부 정보를 나타냄
        // 여기서는 사용자 ID, sns ID, 권한, 비밀번호 등의 정보를 포함
        return UserPrincipal.builder()
                .userId(user.getId())
                .snsId(user.getSnsId())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole().toString())))
                .password(user.getPassword())
                .build();
    }
}