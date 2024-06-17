package com.eumakase.eumakase;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import jakarta.annotation.PostConstruct;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.File;

@SpringBootApplication
@EnableJpaAuditing
@EnableEncryptableProperties
public class EumakaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(EumakaseApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// 로그 디렉토리 생성
		File logDir = new File("logs");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
	}
}
