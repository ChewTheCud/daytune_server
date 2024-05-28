package com.eumakase.eumakase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 코어 스레드 풀 크기 설정
        executor.setMaxPoolSize(50); // 최대 스레드 풀 크기 설정
        executor.setQueueCapacity(100); // 대기열 크기 설정
        executor.setThreadNamePrefix("Async-"); // 스레드 이름 접두사 설정
        executor.initialize();
        return executor;
    }
}