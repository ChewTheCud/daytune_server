package com.eumakase.eumakase.security;

import com.eumakase.eumakase.support.TestAdminCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 관리자 비밀번호 해시가 $2y$ 접두사(htpasswd·PHP 계열 생성기)여도 Spring BCryptPasswordEncoder가 검증하는지 확인한다
 */
class BCryptCompatibilityTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void 해시_2y_를_검증한다() {
        assertThat(encoder.matches(TestAdminCredentials.PASSWORD, TestAdminCredentials.HASH)).isTrue();
        assertThat(encoder.matches("wrong-password", TestAdminCredentials.HASH)).isFalse();
    }

    @Test
    void 운영_로컬_관리자_2y_해시를_BCrypt_형식으로_인식한다() {
        // 평문은 테스트에 두지 않는다. 형식 인식 실패면 upgradeEncoding이 예외를 던지고 matches가 경고 후 즉시 false다
        String prodHash = "$2y$12$0f6DvgwdqBE1ESGA39b70OCC/p1kz2JGulerIS/fBRR7SFAgmf92i";
        String localHash = "$2y$10$r3zwgIESKokF4Ux7YzXp5.YUDDn.IYx2SO/CRwUtEKz1ek3vywaRu";
        assertThat(encoder.upgradeEncoding(prodHash)).isFalse();
        assertThat(encoder.upgradeEncoding(localHash)).isFalse();
        assertThat(encoder.matches("not-the-password", prodHash)).isFalse();
    }
}
