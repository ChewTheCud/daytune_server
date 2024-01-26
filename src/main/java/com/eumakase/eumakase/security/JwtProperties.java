package com.eumakase.eumakase.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.jwt") // security.jwt로 시작하는 프로퍼티들을 이 클래스의 필드와 자동으로 바인딩
public class JwtProperties {
    // JWT를 발행할 때 사용할 비밀 키
    private String secretKey;
}