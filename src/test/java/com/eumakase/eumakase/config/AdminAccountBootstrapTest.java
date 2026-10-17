package com.eumakase.eumakase.config;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.support.TestAdminCredentials;
import com.eumakase.eumakase.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminAccountBootstrapTest {

    private static final String HASH = TestAdminCredentials.HASH;

    private AdminProperties properties;
    private UserRepository userRepository;
    private AdminAccountBootstrap bootstrap;

    @BeforeEach
    void setUp() {
        properties = new AdminProperties();
        userRepository = mock(UserRepository.class);
        bootstrap = new AdminAccountBootstrap(properties, userRepository);
    }

    private void configure(String email, String hash) {
        properties.getBootstrap().setEmail(email);
        properties.getBootstrap().setPasswordHash(hash);
    }

    @Test
    void 관리자가_없으면_설정값으로_생성한다() {
        configure("bootstrap-admin@example.com", HASH);
        when(userRepository.findByEmailAndRole("bootstrap-admin@example.com", Role.ROLE_ADMIN)).thenReturn(List.of());
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        bootstrap.createAdminIfAbsent();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User created = captor.getValue();
        assertThat(created.getEmail()).isEqualTo("bootstrap-admin@example.com");
        assertThat(created.getPassword()).isEqualTo(HASH);
        assertThat(created.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(created.getNickname()).isEqualTo("관리자");
        assertThat(created.getSnsId()).isNull();
    }

    @Test
    void 같은_이메일의_관리자가_있으면_아무것도_바꾸지_않는다() {
        configure("bootstrap-admin@example.com", HASH);
        when(userRepository.findByEmailAndRole("bootstrap-admin@example.com", Role.ROLE_ADMIN))
                .thenReturn(List.of(User.builder().id(5L).password("old-hash").build()));

        bootstrap.createAdminIfAbsent();

        verify(userRepository, never()).save(any());
    }

    @Test
    void 설정값이_하나라도_비면_건너뛴다() {
        configure("bootstrap-admin@example.com", "");
        bootstrap.createAdminIfAbsent();
        configure(null, HASH);
        bootstrap.createAdminIfAbsent();

        verifyNoInteractions(userRepository);
    }

    @Test
    void 실패해도_예외를_던지지_않는다() {
        configure("bootstrap-admin@example.com", HASH);
        when(userRepository.findByEmailAndRole(any(), any())).thenThrow(new RuntimeException("DB down"));

        assertThatCode(() -> bootstrap.createAdminIfAbsent()).doesNotThrowAnyException();
    }
}
