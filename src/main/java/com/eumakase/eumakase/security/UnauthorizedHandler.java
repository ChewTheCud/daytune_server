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
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // HttpServletResponse의 sendError 메서드를 사용하여 HTTP 401 Unauthorized 에러 반환
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}