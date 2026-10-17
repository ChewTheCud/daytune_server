package com.eumakase.eumakase.dto.admin;

import com.eumakase.eumakase.util.enums.ExternalApiOperation;
import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /admin/costs 응답 DTO (외부 API 과금)
 * null 필드도 키를 유지한다 (프론트가 "값 없음"과 "필드 없음"을 구분하지 않도록)
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AdminCostsResponseDto {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime asOf;
    private List<ProviderCost> providers;
    private List<DailyCost> daily;
    private List<OperationCost> topOperations;
    private List<UserCost> topUsers;

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class ProviderCost {
        private ExternalApiProvider provider;
        private long calls;
        private long failures;
        private Long unitsIn;
        private Long unitsOut;
        private BigDecimal estimatedCostUsd;
        private String source; // ESTIMATED | NONE
    }

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class DailyCost {
        private LocalDate date;
        private ExternalApiProvider provider;
        private long calls;
        private BigDecimal estimatedCostUsd;
    }

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class OperationCost {
        private ExternalApiProvider provider;
        private ExternalApiOperation operation;
        private long calls;
        private BigDecimal estimatedCostUsd;
    }

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class UserCost {
        private Long userId;
        private String nickname;
        private long calls;
        private BigDecimal estimatedCostUsd;
    }
}
