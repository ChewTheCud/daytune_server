package com.eumakase.eumakase.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/**") // 모든 경로에 보안 정책 적용
                .sessionManagement(sessionManagementConfigurer
                        -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable) // 폼 기반 로그인 비활성화
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/").permitAll() // 루트 경로("/")에 대한 모든 요청을 허용
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                );
        return http.build(); // 구성된 HttpSecurity 객체 반환
    }
}
