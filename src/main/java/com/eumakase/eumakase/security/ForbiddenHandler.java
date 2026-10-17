package com.eumakase.eumakase.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 됐지만 권한이 없는 요청(예: 일반 유저의 /admin/** 접근)에 403을 반환한다.
 * 이 핸들러가 없으면 /error 디스패치를 거치며 401로 바뀌어 프론트가 권한 부족과 토큰 만료를 구분할 수 없다.
 */
@Component
class ForbiddenHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":\"ERROR\",\"message\":\"권한이 없습니다.\"}");
    }
}
