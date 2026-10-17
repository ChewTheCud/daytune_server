package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.exception.AdminException;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.security.AdminLoginAttemptLimiter;
import com.eumakase.eumakase.security.JwtIssuer;
import com.eumakase.eumakase.support.TestAdminCredentials;
import com.eumakase.eumakase.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminAuthServiceTest {

    private static final String HASH = TestAdminCredentials.HASH;
    private static final String PASSWORD = TestAdminCredentials.PASSWORD;

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtIssuer jwtIssuer;
    private AdminLoginAttemptLimiter limiter;
    private AdminAuthService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = spy(new BCryptPasswordEncoder());
        jwtIssuer = mock(JwtIssuer.class);
        limiter = mock(AdminLoginAttemptLimiter.class);
        service = new AdminAuthService(userRepository, passwordEncoder, jwtIssuer, limiter);
        when(jwtIssuer.issue(anyLong(), any(), any())).thenReturn("token");
    }

    private User admin(LocalDateTime deletedDate) {
        return User.builder().id(1L).email("admin@example.com").password(HASH).role(Role.ROLE_ADMIN).deletedDate(deletedDate).build();
    }

    private HttpStatus statusOf(Runnable call) {
        try {
            call.run();
        } catch (AdminException e) {
            return e.getStatus();
        }
        return HttpStatus.OK;
    }

    @Test
    void 관리자_비밀번호가_맞으면_ROLE_ADMIN_토큰을_발급하고_카운터를_초기화한다() {
        when(userRepository.findByEmailAndRole("admin@example.com", Role.ROLE_ADMIN)).thenReturn(List.of(admin(null)));

        assertThat(service.login("admin@example.com", PASSWORD)).isEqualTo("token");
        verify(jwtIssuer).issue(1L, "admin@example.com", List.of("ROLE_ADMIN"));
        verify(limiter).recordSuccess("admin@example.com");
    }

    @Test
    void 비밀번호_불일치_없는_이메일_탈퇴_관리자는_모두_같은_401이다() {
        when(userRepository.findByEmailAndRole("admin@example.com", Role.ROLE_ADMIN)).thenReturn(List.of(admin(null)));
        when(userRepository.findByEmailAndRole("deleted@example.com", Role.ROLE_ADMIN))
                .thenReturn(List.of(admin(LocalDateTime.now())));
        when(userRepository.findByEmailAndRole("nobody@example.com", Role.ROLE_ADMIN)).thenReturn(List.of());

        assertThat(statusOf(() -> service.login("admin@example.com", "wrong"))).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(statusOf(() -> service.login("nobody@example.com", PASSWORD))).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(statusOf(() -> service.login("deleted@example.com", PASSWORD))).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(limiter, times(3)).recordFailure(anyString());
        verifyNoInteractions(jwtIssuer);
    }

    @Test
    void 없는_이메일도_BCrypt_비교를_수행한다() {
        when(userRepository.findByEmailAndRole(any(), any())).thenReturn(List.of());

        statusOf(() -> service.login("nobody@example.com", "pw"));
        verify(passwordEncoder).matches("pw", AdminAuthService.DUMMY_HASH);
        // 형식이 BCrypt가 아니면 upgradeEncoding이 예외를 던진다
        assertThat(new BCryptPasswordEncoder().upgradeEncoding(AdminAuthService.DUMMY_HASH)).isFalse();
    }

    @Test
    void 잠금_중이면_비밀번호를_확인하지_않고_429다() {
        when(limiter.isLocked("admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.login("admin@example.com", PASSWORD))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        verifyNoInteractions(userRepository);
        verify(passwordEncoder, never()).matches(any(), any());
    }
}
