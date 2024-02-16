package com.eumakase.eumakase.security;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 구성
 */
@Configuration
@EnableWebSecurity // 이 클래스에서 Spring Security 설정을 활성화.
@RequiredArgsConstructor // Lombok 라이브러리를 사용하여 final 또는 @NonNull 필드에 대한 생성자 자동 생성.
public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailService customUserDetailService;
    private final UnauthorizedHandler unauthorizedHandler;

    @Bean
    public SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 추가.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http
                .cors(AbstractHttpConfigurer::disable) // CORS 보호 비활성화.
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화.
                .sessionManagement(sessionManagementConfigurer
                        -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않음을 나타내는 STATELESS 정책 설정.
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화.
                .exceptionHandling((exception)-> exception.authenticationEntryPoint(unauthorizedHandler)) // 인증되지 않은 요청에 대한 처리 핸들러 설정.
                .authorizeHttpRequests(registry -> registry // HTTP 요청에 대한 권한 설정.
                        .requestMatchers("/").permitAll() // 루트 경로에 대한 요청은 모두 허용.
                        .requestMatchers("/api/v1/auth/login/social", "/api/v1/auth/reissue").permitAll() // 로그인 및 회원가입 경로에 대한 요청은 모두 허용.
                        .requestMatchers("/admin/**").hasRole("ADMIN") // '/admin/'으로 시작하는 경로는 'ADMIN' 역할을 가진 사용자만 접근 가능.
                        .anyRequest().authenticated()); // 그 외 모든 요청은 인증된 사용자만 접근 가능.

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화에 BCrypt 알고리즘 사용.
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        // 인증 메커니즘 구성.
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailService) // 사용자 상세 정보 서비스 설정.
                .passwordEncoder(passwordEncoder()) // 비밀번호 암호화 설정.
                .and().build();
    }
}