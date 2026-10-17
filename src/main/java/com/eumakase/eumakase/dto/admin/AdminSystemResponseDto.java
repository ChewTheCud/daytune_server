package com.eumakase.eumakase.dto.admin;

import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /admin/system 응답 DTO (시스템 상태)
 */
@Getter
@AllArgsConstructor
public class AdminSystemResponseDto {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime asOf;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime windowFrom;
    private List<ApiHealth> apis; // 기능 8개 모두 (호출 0건 포함)
    private List<BatchRun> batchRuns; // 최신 10건
    private List<RecentError> recentErrors; // 창 내 최신 20건

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class ApiHealth {
        private ExternalApiProvider provider;
        private ExternalApiOperation operation;
        private long calls;
        private long failures;
        private double failureRate; // 0~1, 호출 0건이면 0
        private Integer p95LatencyMs; // 호출 0건이면 null
    }

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class BatchRun {
        private String jobName;
        private String status;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;
        private String exitCode;
    }

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class RecentError {
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdDate;
        private ExternalApiProvider provider;
        private ExternalApiOperation operation;
        private String errorCode;
    }
}
