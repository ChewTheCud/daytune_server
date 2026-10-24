package com.eumakase.eumakase.common.constant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * OpenAI 토큰 단가 (Standard, USD / 1M 토큰)
 * - 출처: https://developers.openai.com/api/docs/pricing
 * - 확인일: 2026-10-24
 * - 단가가 바뀌면 이 표를 고친다. 이미 기록된 행의 비용은 기록 시점 값으로 남는다
 * - 응답의 model 값(실제 처리 모델)과 정확히 일치할 때만 계산한다. 날짜 스냅샷마다 가격이 다를 수 있어 접두사로 추정하지 않는다
 */
public final class OpenAiPricing {

    private static final BigDecimal ONE_MILLION = BigDecimal.valueOf(1_000_000);

    // model → {입력 단가, 출력 단가}
    private static final Map<String, BigDecimal[]> PRICES = Map.of(
            "gpt-4-turbo-2024-04-09", new BigDecimal[]{new BigDecimal("10.00"), new BigDecimal("30.00")},
            // 별칭. 가격표에는 스냅샷(gpt-4-turbo-2024-04-09)만 있고 별칭은 이 스냅샷을 가리킨다
            "gpt-4-turbo", new BigDecimal[]{new BigDecimal("10.00"), new BigDecimal("30.00")},
            // 2026-10-23 gpt-4-turbo 종료 이후 운영 모델. Standard, 입력 27.2만 토큰 이하
            "gpt-5.6-terra", new BigDecimal[]{new BigDecimal("2.00"), new BigDecimal("12.00")}
    );

    private OpenAiPricing() {
    }

    /**
     * @return 추정 비용(USD, 소수 6자리). 단가를 모르는 모델이거나 토큰 수가 없으면 null
     */
    public static BigDecimal estimate(String model, Long promptTokens, Long completionTokens) {
        if (model == null || promptTokens == null || completionTokens == null) {
            return null;
        }
        BigDecimal[] price = PRICES.get(model);
        if (price == null) {
            return null;
        }
        return price[0].multiply(BigDecimal.valueOf(promptTokens))
                .add(price[1].multiply(BigDecimal.valueOf(completionTokens)))
                .divide(ONE_MILLION, 6, RoundingMode.HALF_UP);
    }
}
