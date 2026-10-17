package com.eumakase.eumakase.common.constant;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiPricingTest {

    @Test
    void gpt_4_turbo_스냅샷은_입력10_출력30_달러_per_1M으로_계산한다() {
        // 1000 × 10 / 1M + 300 × 30 / 1M = 0.01 + 0.009
        assertThat(OpenAiPricing.estimate("gpt-4-turbo-2024-04-09", 1000L, 300L))
                .isEqualByComparingTo(new BigDecimal("0.019000"));
    }

    @Test
    void 별칭_gpt_4_turbo도_같은_단가다() {
        assertThat(OpenAiPricing.estimate("gpt-4-turbo", 1L, 1L)).isEqualByComparingTo(new BigDecimal("0.000040"));
    }

    @Test
    void 결과는_소수_6자리다() {
        assertThat(OpenAiPricing.estimate("gpt-4-turbo", 1L, 0L).scale()).isEqualTo(6);
    }

    @Test
    void 단가표에_없는_모델은_접두사가_같아도_null이다() {
        assertThat(OpenAiPricing.estimate("gpt-4-turbo-preview", 100L, 100L)).isNull();
        assertThat(OpenAiPricing.estimate("gpt-4o-2024-08-06", 100L, 100L)).isNull();
    }

    @Test
    void 모델이나_토큰_수가_없으면_null이다() {
        assertThat(OpenAiPricing.estimate(null, 1L, 1L)).isNull();
        assertThat(OpenAiPricing.estimate("gpt-4-turbo", null, 1L)).isNull();
        assertThat(OpenAiPricing.estimate("gpt-4-turbo", 1L, null)).isNull();
    }
}
