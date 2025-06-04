package com.eumakase.eumakase.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 배치 작업 스케줄링
 */
@Configuration
@EnableScheduling // 스케줄링 활성화
public class SchedulerConfig {

    private final JobLauncher jobLauncher;
    private final Job updateMusicFileUrlsJob;

    /**
     * @param jobLauncher Job을 실행하는데 사용
     * @param updateMusicFileUrlsJob 스케줄링할 Job
     */
    public SchedulerConfig(JobLauncher jobLauncher, Job updateMusicFileUrlsJob) {
        this.jobLauncher = jobLauncher;
        this.updateMusicFileUrlsJob = updateMusicFileUrlsJob;
    }

    /**
     * updateMusicFileUrlsJob 스케줄링
     * 테스트용: 매 1분마다 실행
     */
    @Scheduled(cron = "0 * * * * *")
    public void perform() {
        try {
            jobLauncher.run(
                    updateMusicFileUrlsJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis()) // Job 파라미터에 현재 시간을 추가
                            .toJobParameters()
            );
        } catch (Exception e) {
            System.err.println("Job execution failed: " + e.getMessage());
        }
    }
}
