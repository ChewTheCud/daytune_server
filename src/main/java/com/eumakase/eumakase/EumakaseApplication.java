package com.eumakase.eumakase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EumakaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(EumakaseApplication.class, args);
	}

}
