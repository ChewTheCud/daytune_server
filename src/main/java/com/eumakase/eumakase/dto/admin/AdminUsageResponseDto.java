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
    private Funnel funnel; // 기간 내 가입 코호트 기준 퍼널
    private List<EmotionCount> emotions; // count 내림차순 상위 10
    private List<CategoryCount> categories; // count 내림차순, 카테고리 없는 일기는 categoryId null
    private List<HeatmapCell> heatmap; // 요일 1~7 × 시간 0~23, 168칸 (0 채움)

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

    @Getter
    @AllArgsConstructor
    public static class Funnel {
        private long signedUp;
        private long wroteDiary;
        private long generatedMusic;
        private long shared;
    }

    @Getter
    @AllArgsConstructor
    public static class EmotionCount {
        private String emotion;
        private long count;
    }

    @Getter
    @AllArgsConstructor
    public static class CategoryCount {
        private Long categoryId;
        private String name;
        private long count;
    }

    @Getter
    @AllArgsConstructor
    public static class HeatmapCell {
        private int dayOfWeek; // 1=월 ~ 7=일
        private int hour; // 0~23
        private long count;
    }
}
