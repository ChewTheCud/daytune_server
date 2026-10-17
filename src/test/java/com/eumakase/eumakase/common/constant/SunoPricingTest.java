package com.eumakase.eumakase.common.constant;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SunoPricingTest {

    private static final BigDecimal PER_CREDIT = new BigDecimal("0.005");

    @Test
    void 크레딧_수와_단가를_곱해_소수_6자리로_반환한다() {
        BigDecimal cost = SunoPricing.estimate(12L, PER_CREDIT);
        assertThat(cost).isEqualByComparingTo("0.06");
        assertThat(cost.scale()).isEqualTo(6);
    }

    @Test
    void 크레딧_수나_단가가_없거나_음수면_null이다() {
        assertThat(SunoPricing.estimate(null, PER_CREDIT)).isNull();
        assertThat(SunoPricing.estimate(10L, null)).isNull();
        assertThat(SunoPricing.estimate(-1L, PER_CREDIT)).isNull();
    }

    @Test
    void 전후_차이가_양수면_실측값을_쓴다() {
        assertThat(SunoPricing.resolveCredits(100L, 88L, 10)).isEqualTo(12L);
    }

    @Test
    void 차이가_0_이하이거나_조회_실패면_설정값으로_대체한다() {
        assertThat(SunoPricing.resolveCredits(100L, 100L, 10)).isEqualTo(10L);
        assertThat(SunoPricing.resolveCredits(100L, 120L, 10)).isEqualTo(10L);
        assertThat(SunoPricing.resolveCredits(null, 88L, 10)).isEqualTo(10L);
        assertThat(SunoPricing.resolveCredits(100L, null, 10)).isEqualTo(10L);
    }

    @Test
    void 대체할_설정값도_없으면_null이다() {
        assertThat(SunoPricing.resolveCredits(null, null, null)).isNull();
        assertThat(SunoPricing.resolveCredits(100L, 100L, null)).isNull();
    }
}
