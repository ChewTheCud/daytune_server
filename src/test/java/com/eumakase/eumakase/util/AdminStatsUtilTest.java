package com.eumakase.eumakase.util;

import com.eumakase.eumakase.exception.AdminException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminStatsUtilTest {

    private static final LocalDate D = LocalDate.of(2026, 10, 1);

    @Test
    void 기간_92일은_허용하고_93일은_400이다() {
        AdminStatsUtil.validateRange(D, D.plusDays(91));
        assertThatThrownBy(() -> AdminStatsUtil.validateRange(D, D.plusDays(92)))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 같은_날은_허용하고_역순은_400이다() {
        AdminStatsUtil.validateRange(D, D);
        assertThatThrownBy(() -> AdminStatsUtil.validateRange(D, D.minusDays(1)))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 날짜_목록은_양_끝을_포함한다() {
        assertThat(AdminStatsUtil.datesBetween(D, D.plusDays(2)))
                .containsExactly(D, D.plusDays(1), D.plusDays(2));
    }

    @Test
    void 빈_날짜는_0으로_채울_수_있다() {
        Map<LocalDate, Long> counts = AdminStatsUtil.toDailyCounts(List.<Object[]>of(new Object[]{D.plusDays(1), 3L}));
        List<Long> series = AdminStatsUtil.datesBetween(D, D.plusDays(2)).stream()
                .map(date -> counts.getOrDefault(date, 0L))
                .toList();
        assertThat(series).containsExactly(0L, 3L, 0L);
    }

    @Test
    void p95는_nearest_rank다() {
        // 1..100 → ceil(95) = 95번째
        List<Integer> values = new java.util.ArrayList<>();
        for (int i = 100; i >= 1; i--) {
            values.add(i);
        }
        assertThat(AdminStatsUtil.p95(values)).isEqualTo(95);
        // 1..10 → ceil(9.5) = 10번째
        assertThat(AdminStatsUtil.p95(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))).isEqualTo(10);
        assertThat(AdminStatsUtil.p95(List.of(42))).isEqualTo(42);
    }

    @Test
    void p95는_값이_없으면_null이다() {
        assertThat(AdminStatsUtil.p95(List.of())).isNull();
        assertThat(AdminStatsUtil.p95(null)).isNull();
    }

    @Test
    void 실패율은_소수_4자리이고_0건이면_0이다() {
        assertThat(AdminStatsUtil.failureRate(1, 3)).isEqualTo(0.3333);
        assertThat(AdminStatsUtil.failureRate(0, 0)).isEqualTo(0.0);
        assertThat(AdminStatsUtil.failureRate(5, 5)).isEqualTo(1.0);
    }
}
