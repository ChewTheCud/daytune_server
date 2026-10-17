package com.eumakase.eumakase.util;

import com.eumakase.eumakase.dto.admin.AdminUsageResponseDto;
import com.eumakase.eumakase.exception.AdminException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 집계 공통 계산 (스프링 의존 없음)
 */
public final class AdminStatsUtil {

    public static final int MAX_RANGE_DAYS = 92;

    private AdminStatsUtil() {
    }

    /**
     * from~to(양 끝 포함) 검증. 역순이거나 92일 초과면 400
     */
    public static void validateRange(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "to는 from보다 이전일 수 없습니다.");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "조회 기간은 최대 " + MAX_RANGE_DAYS + "일입니다.");
        }
    }

    /**
     * from~to(양 끝 포함)의 모든 날짜. 일별 시리즈의 빈 날짜를 0으로 채우는 기준이다
     */
    public static List<LocalDate> datesBetween(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }

    /**
     * [date, count] 행 → 날짜별 맵. 없는 날짜는 getOrDefault(date, 0L)로 0 처리
     */
    public static Map<LocalDate, Long> toDailyCounts(List<Object[]> rows) {
        Map<LocalDate, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put((LocalDate) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    /**
     * p95 (nearest-rank: 오름차순 정렬 후 ceil(0.95 × n)번째 값). 값이 없으면 null
     */
    public static Integer p95(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Integer> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int rank = (int) Math.ceil(0.95 * sorted.size());
        return sorted.get(rank - 1);
    }

    /**
     * 실패율 0~1 (소수 4자리 반올림). 호출 0건이면 0
     */
    public static double failureRate(long failures, long calls) {
        if (calls == 0) {
            return 0;
        }
        return Math.round((double) failures / calls * 10_000) / 10_000.0;
    }

    /**
     * 작성 시각 목록 → 요일(1=월~7=일) × 시간(0~23) 168칸. 빈 칸은 0, dayOfWeek → hour 오름차순
     */
    public static List<AdminUsageResponseDto.HeatmapCell> buildHeatmap(List<LocalDateTime> times) {
        long[][] counts = new long[7][24];
        for (LocalDateTime time : times) {
            counts[time.getDayOfWeek().getValue() - 1][time.getHour()]++;
        }
        List<AdminUsageResponseDto.HeatmapCell> cells = new ArrayList<>(7 * 24);
        for (int day = 0; day < 7; day++) {
            for (int hour = 0; hour < 24; hour++) {
                cells.add(new AdminUsageResponseDto.HeatmapCell(day + 1, hour, counts[day][hour]));
            }
        }
        return cells;
    }
}
