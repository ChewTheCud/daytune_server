package com.eumakase.eumakase;

import com.eumakase.eumakase.config.batch.SchedulerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트가 운영 설정과 격리돼 있는지 확인한다.
 * 이 테스트가 실패하면 src/test/resources/application.yml이 main의 설정을 가리지 못한 것이다.
 */
@SpringBootTest
class TestIsolationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void 활성_프로필은_test_하나뿐이다() {
        assertThat(environment.getActiveProfiles())
                .as("활성 프로필이 [test]가 아니다. 운영(prod) 설정이 로드됐을 수 있다")
                .containsExactly("test");
    }

    @Test
    void 데이터소스는_H2_인메모리다() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL())
                    .as("DataSource가 H2가 아니다. 운영 DB 설정이 로드됐을 수 있다")
                    .startsWith("jdbc:h2:");
        }
    }

    @Test
    void 스케줄러는_비활성이다() {
        assertThat(applicationContext.getBeanNamesForType(SchedulerConfig.class))
                .as("SchedulerConfig 빈이 존재한다. 운영 설정이 로드됐거나 scheduling.enabled가 꺼지지 않아 실제 Suno API가 호출될 수 있다")
                .isEmpty();
    }
}
