package com.eumakase.eumakase.support;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TestIsolationGuard가 운영 설정 유입을 기동 전에 차단하는지 확인한다.
 */
class TestIsolationGuardTest {

    private final TestIsolationGuard guard = new TestIsolationGuard();

    private MockEnvironment environment(String profile, String datasourceUrl) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        if (datasourceUrl != null) {
            environment.setProperty("spring.datasource.url", datasourceUrl);
        }
        return environment;
    }

    @Test
    void prod_프로필이면_기동을_중단한다() {
        MockEnvironment environment = environment("prod", "jdbc:h2:mem:daytune");

        assertThatThrownBy(() -> guard.postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("활성 프로필");
    }

    @Test
    void H2가_아닌_DB_URL이면_기동을_중단한다() {
        MockEnvironment environment = environment("test", "jdbc:mysql://db.example.com:3306/daytune");

        assertThatThrownBy(() -> guard.postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.datasource.url");
    }

    @Test
    void 암호화된_URL이면_중단하고_메시지에_값을_노출하지_않는다() {
        String encrypted = "ENC(abcdefSECRETVALUE123)";
        MockEnvironment environment = environment("test", encrypted);

        assertThatThrownBy(() -> guard.postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .message()
                .doesNotContain(encrypted)
                .doesNotContain("SECRETVALUE");
    }

    @Test
    void test_프로필과_H2_URL이면_통과한다() {
        MockEnvironment environment = environment("test", "jdbc:h2:mem:daytune;MODE=MySQL");

        assertThatCode(() -> guard.postProcessEnvironment(environment, new SpringApplication()))
                .doesNotThrowAnyException();
    }
}
