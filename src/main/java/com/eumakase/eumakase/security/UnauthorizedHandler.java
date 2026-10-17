package com.eumakase.eumakase.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security의 AuthenticationEntryPoint 인터페이스 구현 클래스
 */
// 인증되지 않은 요청이 들어올 때 실행되는 메서드
@Component
class UnauthorizedHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeUnauthorized(response);
    }

    // 토큰 없음(entry point)과 토큰 만료·위조(JwtAuthenticationFilter)가 같은 401 본문을 쓰도록 공유한다
    static void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\": \"ERROR\", \"message\": \"Access Token 값이 유효하지 않습니다.\"}");
    }
}