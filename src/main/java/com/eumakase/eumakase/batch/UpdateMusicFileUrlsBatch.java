package com.eumakase.eumakase.batch;

import com.eumakase.eumakase.service.MusicService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * updateMusicFileUrlsJob과 관련된 배치 작업 정의
 */
@Configuration
@EnableBatchProcessing
public class UpdateMusicFileUrlsBatch {

    private final MusicService musicService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public UpdateMusicFileUrlsBatch(MusicService musicService, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.musicService = musicService;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    /**
     * 하나의 Step을 포함
     * @return updateMusicFileUrlsJob
     */
    @Bean
    public Job updateMusicFileUrlsJob() {
        return new JobBuilder("updateMusicFileUrlsJob", jobRepository)
                .start(updateMusicFileUrlsStep())
                .build();
    }

    /**
     * Tasklet을 사용하여 음악 파일 URL을 업데이트
     * @return updateMusicFileUrlsStep
     */
    @Bean
    public Step updateMusicFileUrlsStep() {
        Tasklet tasklet = (contribution, chunkContext) -> {
            musicService.updateMusicFileUrls(); // 음악 파일 URL을 업데이트하는 서비스 호출
            return RepeatStatus.FINISHED; // 작업이 완료되었음을 나타냄
        };

        TaskletStepBuilder stepBuilder = new StepBuilder("updateMusicFileUrlsStep", jobRepository)
                .tasklet(tasklet, transactionManager); // Tasklet과 트랜잭션 매니저를 사용하여 Step을 구성

        return stepBuilder.build();
    }
}
