package com.eumakase.eumakase.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 기능 설정 (admin.*)
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {
    private Bootstrap bootstrap = new Bootstrap();
    private Cors cors = new Cors();

    /**
     * 기동 시 관리자 계정이 없으면 만들 계정. 비밀번호는 BCrypt 해시만 둔다 (공개 저장소)
     */
    @Getter
    @Setter
    public static class Bootstrap {
        private String email;
        private String passwordHash;
    }

    /**
     * /admin/** 에만 적용하는 CORS 허용 오리진. 비어 있으면 모든 교차 출처 요청을 거부한다
     */
    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }
}
