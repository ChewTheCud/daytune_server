package com.eumakase.eumakase.dto.admin;

import com.eumakase.eumakase.util.enums.ExternalApiProvider;
import com.eumakase.eumakase.util.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /admin/users/{userId} 응답 DTO (유저 상세)
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AdminUserDetailResponseDto {
    private Long id;
    private String nickname;
    private String email;
    private Role role;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedDate;
    private Counts counts; // 전체 기간
    private List<Activity> recentActivity; // 일기·음악 최신 20건
    private List<ProviderCost> costs; // 기간 내, 제공사 4개 모두

    @Getter
    @AllArgsConstructor
    public static class Counts {
        private long diaries;
        private long musics;
        private long shares;
    }

    @Getter
    @AllArgsConstructor
    public static class Activity {
        private String type; // DIARY | MUSIC
        private Long id;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class ProviderCost {
        private ExternalApiProvider provider;
        private long calls;
        private BigDecimal estimatedCostUsd;
    }
}
