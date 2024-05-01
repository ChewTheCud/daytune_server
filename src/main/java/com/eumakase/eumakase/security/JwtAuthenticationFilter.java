package com.eumakase.eumakase.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Spring Security의 OncePerRequestFilter를 상속하여 JWT를 사용하여 인증을 처리하는 필터
 * 여러 번 호출되는 것을 방지하는 OncePerRequestFilter를 상속하므로 각 HTTP 요청에 대해 한 번만 실행
 * 모든 HTTP 요청에서 JWT를 추출하고 디코딩하여 UserPrincipalAuthenticationToken으로 변환한 후, Spring Security의 SecurityContextHolder에 설정.
 * 이를 통해 각 요청에 대한 사용자 정보를 Spring Security에서 활용할 수 있도록 함.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtDecoder jwtDecoder; // JWT 디코드를 위한 컴포넌트
    private final JwtToPrincipalConverter jwtToPrincipalConverter; // JWT에서 UserPrincipal로 변환하는 컴포넌트

    // HTTP 요청에 대한 JWT 인증 필터
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            extractTokenFromRequest(request) // 요청으로부터 JWT 추출
                    .map(jwtDecoder::decode) // JWT 디코드
                    .map(jwtToPrincipalConverter::convert) // 디코드된 JWT를 UserPrincipal 객체로 변환
                    .map(UserPrincipalAuthenticationToken::new) // UserPrincipal 객체를 사용하여 인증 토큰 생성
                    .ifPresent(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication)); // 생성된 인증 토큰을 보안 컨텍스트에 설정

            filterChain.doFilter(request, response); // 다음 필터로 요청과 응답 전달
        } catch (Exception ex) {
            // Spring Security에 예외 처리를 위임 (AuthenticationEntryPoint로 처리)
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        }
    }

    // HTTP 요청 헤더로부터 JWT 추출
    private Optional<String> extractTokenFromRequest(HttpServletRequest request) {
        var token = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 추출
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return Optional.of(token.substring(7)); // "Bearer " 이후의 문자열을 추출하여 반환
        }
        return Optional.empty(); // 토큰이 없거나 유효하지 않은 경우 빈 Optional 반환
    }
}