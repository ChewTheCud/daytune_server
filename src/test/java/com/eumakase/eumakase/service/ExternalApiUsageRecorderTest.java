package com.eumakase.eumakase.service;

import com.amazonaws.AmazonServiceException;
import com.eumakase.eumakase.domain.ExternalApiUsage;
import com.eumakase.eumakase.repository.ExternalApiUsageRepository;
import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import com.eumakase.eumakase.util.enums.ExternalApiRefType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExternalApiUsageRecorderTest {

    private ExternalApiUsageRepository repository;
    private ExternalApiUsageRecorder recorder;

    @BeforeEach
    void setUp() {
        repository = mock(ExternalApiUsageRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        recorder = new ExternalApiUsageRecorder(repository, transactionManager);
    }

    private ExternalApiUsage saved() {
        ArgumentCaptor<ExternalApiUsage> captor = ArgumentCaptor.forClass(ExternalApiUsage.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void OpenAI_성공은_토큰과_추정_비용을_기록한다() {
        recorder.record(ExternalApiOperation.LYRICS_GENERATE, System.nanoTime(), 7L, null, null,
                "gpt-4-turbo-2024-04-09", 1000L, 300L, null);

        ExternalApiUsage usage = saved();
        assertThat(usage.getProvider()).isEqualTo(ExternalApiProvider.OPENAI);
        assertThat(usage.isSuccess()).isTrue();
        assertThat(usage.getUserId()).isEqualTo(7L);
        assertThat(usage.getUnitsIn()).isEqualTo(1000L);
        assertThat(usage.getUnitsOut()).isEqualTo(300L);
        assertThat(usage.getUnitType()).isEqualTo("TOKEN");
        assertThat(usage.getEstimatedCostUsd()).isEqualByComparingTo(new BigDecimal("0.019"));
        assertThat(usage.getLatencyMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void OpenAI_실패는_비용을_남기지_않는다() {
        recorder.record(ExternalApiOperation.FOLLOW_UP_QUESTION, System.nanoTime(), null, null, null,
                "gpt-4-turbo", null, null, "HTTP_429");

        ExternalApiUsage usage = saved();
        assertThat(usage.isSuccess()).isFalse();
        assertThat(usage.getErrorCode()).isEqualTo("HTTP_429");
        assertThat(usage.getEstimatedCostUsd()).isNull();
        assertThat(usage.getUnitType()).isNull();
    }

    @Test
    void OpenAI가_아닌_제공사는_비용을_산정하지_않는다() {
        recorder.record(ExternalApiOperation.MUSIC_GENERATE, System.nanoTime(), 3L, ExternalApiRefType.DIARY, 11L,
                "V4_5", null, null, null);

        ExternalApiUsage usage = saved();
        assertThat(usage.getProvider()).isEqualTo(ExternalApiProvider.SUNO);
        assertThat(usage.getRefType()).isEqualTo(ExternalApiRefType.DIARY);
        assertThat(usage.getRefId()).isEqualTo(11L);
        assertThat(usage.getEstimatedCostUsd()).isNull();
    }

    @Test
    void 저장이_실패해도_예외를_던지지_않는다() {
        when(repository.save(any())).thenThrow(new RuntimeException("DB down"));

        assertThatCode(() -> recorder.success(ExternalApiOperation.S3_UPLOAD, System.nanoTime()))
                .doesNotThrowAnyException();
        assertThatCode(() -> recorder.failure(ExternalApiOperation.PUSH_SEND, System.nanoTime(), new IllegalStateException()))
                .doesNotThrowAnyException();
    }

    @Test
    void 긴_error_code와_model은_60자로_자른다() {
        recorder.record(ExternalApiOperation.EMOTION_INSIGHT, System.nanoTime(), null, null, null,
                "m".repeat(80), null, null, "e".repeat(80));

        ExternalApiUsage usage = saved();
        assertThat(usage.getModel()).hasSize(60);
        assertThat(usage.getErrorCode()).hasSize(60);
    }

    @Test
    void error_code는_HTTP_상태를_우선하고_메시지는_쓰지_않는다() {
        HttpClientErrorException http = HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests", null, "sk-secret-in-body".getBytes(), null);
        assertThat(ExternalApiUsageRecorder.errorCode(new Exception("wrap: sk-secret", http))).isEqualTo("HTTP_429");

        AmazonServiceException aws = new AmazonServiceException("Access Denied");
        aws.setStatusCode(403);
        assertThat(ExternalApiUsageRecorder.errorCode(aws)).isEqualTo("HTTP_403");
    }

    @Test
    void error_code는_HTTP_상태가_없으면_가장_안쪽_원인의_클래스명이다() {
        ResourceAccessException timeout = new ResourceAccessException("I/O error", new SocketTimeoutException("Read timed out"));
        assertThat(ExternalApiUsageRecorder.errorCode(timeout)).isEqualTo("SocketTimeoutException");
        assertThat(ExternalApiUsageRecorder.errorCode(new IllegalStateException("x"))).isEqualTo("IllegalStateException");
        assertThat(ExternalApiUsageRecorder.errorCode(null)).isEqualTo("UNKNOWN");
    }
}
