package com.eumakase.eumakase.config;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.util.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 기동 완료 시 관리자 계정을 부트스트랩한다.
 * - admin.bootstrap.email·password-hash가 모두 있고, 그 이메일의 ROLE_ADMIN 유저가 없을 때만 INSERT 한다
 * - 기존 행은 절대 수정하지 않는다 (비밀번호를 바꾸려면 DB에서 직접 처리)
 * - 실패해도 기동을 막지 않는다. 로그만 남긴다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountBootstrap {

    static final String ADMIN_NICKNAME = "관리자";

    private final AdminProperties adminProperties;
    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void createAdminIfAbsent() {
        try {
            String email = adminProperties.getBootstrap().getEmail();
            String passwordHash = adminProperties.getBootstrap().getPasswordHash();
            if (!StringUtils.hasText(email) || !StringUtils.hasText(passwordHash)) {
                return;
            }
            if (!userRepository.findByEmailAndRole(email, Role.ROLE_ADMIN).isEmpty()) {
                return;
            }
            User admin = userRepository.save(User.builder()
                    .email(email)
                    .password(passwordHash)
                    .nickname(ADMIN_NICKNAME)
                    .role(Role.ROLE_ADMIN)
                    .build());
            log.info("[관리자 부트스트랩] 관리자 계정을 생성했습니다: userId={}", admin.getId());
        } catch (Exception e) {
            log.error("[관리자 부트스트랩] 관리자 계정 생성에 실패했습니다: {}", e.getClass().getSimpleName(), e);
        }
    }
}
