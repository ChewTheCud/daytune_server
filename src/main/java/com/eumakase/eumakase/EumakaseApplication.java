package com.eumakase.eumakase;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.File;

@SpringBootApplication
@EnableJpaAuditing
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
