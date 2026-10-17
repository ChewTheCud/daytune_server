package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.exception.AdminException;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.security.AdminLoginAttemptLimiter;
import com.eumakase.eumakase.security.JwtIssuer;
import com.eumakase.eumakase.util.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 이메일·비밀번호 로그인. 리프레시 토큰은 없다 (만료 시 재로그인)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    static final String LOGIN_FAILED_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";
    static final String LOCKED_MESSAGE = "로그인 시도가 너무 많습니다. 잠시 후 다시 시도하세요.";
    // 계정이 없을 때도 BCrypt 비교를 한 번 수행해 응답 시간으로 계정 존재 여부가 드러나지 않게 한다 (평문 의미 없음)
    static final String DUMMY_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BWX4Z3mLMvPqD8zqJ3vZ8J1x2e3u";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final AdminLoginAttemptLimiter attemptLimiter;

    /**
     * @return 관리자 액세스 토큰 (JWT, ROLE_ADMIN)
     * @throws AdminException 401(이메일 없음·비밀번호 불일치·비관리자 구분 없음), 429(잠금 중)
     */
    public String login(String email, String password) {
        if (attemptLimiter.isLocked(email)) {
            throw new AdminException(HttpStatus.TOO_MANY_REQUESTS, LOCKED_MESSAGE);
        }

        Optional<User> admin = findActiveAdmin(email);
        String hash = admin.map(User::getPassword).filter(h -> h != null && !h.isBlank()).orElse(DUMMY_HASH);
        boolean matches = passwordEncoder.matches(password, hash);
        if (admin.isEmpty() || !matches) {
            attemptLimiter.recordFailure(email);
            log.warn("[관리자 로그인] 실패");
            throw new AdminException(HttpStatus.UNAUTHORIZED, LOGIN_FAILED_MESSAGE);
        }

        attemptLimiter.recordSuccess(email);
        User user = admin.get();
        log.info("[관리자 로그인] 성공: userId={}", user.getId());
        return jwtIssuer.issue(user.getId(), user.getEmail(), List.of(Role.ROLE_ADMIN.name()));
    }

    private Optional<User> findActiveAdmin(String email) {
        return userRepository.findByEmailAndRole(email.trim(), Role.ROLE_ADMIN).stream()
                .filter(user -> user.getDeletedDate() == null)
                .findFirst();
    }
}
