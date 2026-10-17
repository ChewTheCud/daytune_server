package com.eumakase.eumakase.security;


import com.eumakase.eumakase.config.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
    private final ForbiddenHandler forbiddenHandler;
    private final AdminProperties adminProperties;

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new UnauthorizedHandler();
    }

    @Bean
    public SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 추가.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http
                .cors(Customizer.withDefaults()) // corsConfigurationSource 빈 사용 (/admin/** 에만 적용).
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화.
                .sessionManagement(sessionManagementConfigurer
                        -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않음을 나타내는 STATELESS 정책 설정.
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화.
                .exceptionHandling((exception)-> exception
                        .authenticationEntryPoint(authenticationEntryPoint()) // 인증되지 않은 요청에 대한 처리 핸들러 설정.
                        .accessDeniedHandler(forbiddenHandler)) // 권한 없는 요청은 401이 아닌 403으로 응답.
                .authorizeHttpRequests(registry -> registry // HTTP 요청에 대한 권한 설정.
                        .requestMatchers("/").permitAll() // 루트 경로에 대한 요청은 모두 허용.
                        .requestMatchers("/error").permitAll() // 에러 디스패치가 인증에 막혀 403·500이 401로 바뀌지 않도록 허용.
                        .requestMatchers("/api/v2/auth/social", "/api/v2/auth/reissue", "/api/v2/music/urls").permitAll() // 로그인 및 회원가입 경로에 대한 요청은 모두 허용.
                        .requestMatchers(HttpMethod.POST, "/admin/auth/login").permitAll() // 관리자 로그인은 '/admin/**' 규칙보다 먼저 허용.
                        .requestMatchers("/admin/**").hasRole("ADMIN") // '/admin/'으로 시작하는 경로는 'ADMIN' 역할을 가진 사용자만 접근 가능.
                        .anyRequest().authenticated()); // 그 외 모든 요청은 인증된 사용자만 접근 가능.

        return http.build();
    }

    /**
     * 관리자 프론트(별도 도메인)용 CORS. /admin/** 에만 등록하고, 앱 API 경로에는 CORS 헤더를 내보내지 않는다.
     * 허용 오리진이 비어 있으면 모든 교차 출처 요청이 거부된다. 쿠키를 쓰지 않으므로 credentials는 허용하지 않는다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(adminProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/admin/**", configuration);
        return source;
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