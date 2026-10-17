package com.eumakase.eumakase.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;

/**
 * 테스트 컨텍스트가 운영 설정으로 뜨는 것을 기동 전에 차단한다.
 *
 * src/test/resources/application.yml이 main 설정을 가려도, 환경변수(SPRING_PROFILES_ACTIVE 등)나
 * -D 옵션은 그보다 우선한다. 이 경우 TestIsolationTest가 실패하기 전에 운영 DB 접속이 일어나므로,
 * 설정 로딩 직후(Jasypt 복호화·DataSource 생성 전)에 검사해 기동을 중단한다.
 * 등록: src/test/resources/META-INF/spring.factories (테스트 클래스패스 전용)
 */
public class TestIsolationGuard implements EnvironmentPostProcessor, Ordered {

    static final String H2_URL_PREFIX = "jdbc:h2:mem:";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String[] activeProfiles = environment.getActiveProfiles();
        if (!Arrays.equals(activeProfiles, new String[]{"test"})) {
            throw new IllegalStateException(
                    "테스트 격리 위반: 활성 프로필이 [test]가 아니다 " + Arrays.toString(activeProfiles)
                            + ". 운영 설정이 로드될 수 있어 기동을 중단한다. SPRING_PROFILES_ACTIVE·-Dspring.profiles.active를 확인하라.");
        }

        // URL 값은 메시지에 넣지 않는다 — 운영 설정이 섞였다면 ENC(...) 원문일 수 있다
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        if (datasourceUrl == null || !datasourceUrl.startsWith(H2_URL_PREFIX)) {
            throw new IllegalStateException(
                    "테스트 격리 위반: spring.datasource.url이 " + H2_URL_PREFIX
                            + "로 시작하지 않는다. 운영 DB 접속을 막기 위해 기동을 중단한다. SPRING_DATASOURCE_URL·SPRING_CONFIG_LOCATION을 확인하라.");
        }
    }

    @Override
    public int getOrder() {
        // ConfigDataEnvironmentPostProcessor(설정 파일 로딩) 이후에 실행돼야 최종 값을 본다
        return Ordered.LOWEST_PRECEDENCE;
    }
}
