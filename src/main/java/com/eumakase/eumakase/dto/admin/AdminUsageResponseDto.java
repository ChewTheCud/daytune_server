package com.eumakase.eumakase.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * GET /admin/usage 응답 DTO (이용 현황)
 */
@Getter
@AllArgsConstructor
public class AdminUsageResponseDto {
    private long totalUsers; // 조회 시각 기준 누적, 탈퇴 제외
    private Counts totals; // 기간 합계 (activeUsers는 기간 전체 중복 제거)
    private List<Daily> daily; // from~to 모든 날짜, 빈 날짜는 0

    @Getter
    @AllArgsConstructor
    public static class Counts {
        private long signups;
        private long activeUsers;
        private long diaries;
        private long musics;
        private long shares;
        private long pushes;
    }

    @Getter
    @AllArgsConstructor
    public static class Daily {
        private LocalDate date;
        private long signups;
        private long activeUsers;
        private long diaries;
        private long musics;
        private long shares;
        private long pushes;
    }
}
