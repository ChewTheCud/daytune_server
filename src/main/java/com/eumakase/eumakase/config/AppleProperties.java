package com.eumakase.eumakase.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "apple")
public class AppleProperties {
    private String grantType;
    private String clientId;
    private String keyId;
    private String teamId;
    private String audience;
    private String privateKey;
}