package com.eumakase.eumakase.common.constant;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Suno 비용 추정 (크레딧 기반)
 * - 크레딧 1개의 가치: $0.005 USD (출처: https://sunoapi.org/ko/billing, 확인일: 2026-10-18)
 * - 생성 1회당 크레딧 수는 공개 페이지에서 확인되지 않아, 생성 전후 잔여 크레딧 차이로 실측한다(근사).
 *   조회 실패·차이가 0 이하이면 설정값(metering.suno.credits-per-generate)으로 대체한다
 * - 근사인 이유: 다른 생성 요청이 겹치면 그 소모분이 차이에 섞인다
 */
public final class SunoPricing {

    private SunoPricing() {
    }

    /**
     * 생성 1회 소모 크레딧. 전후 차이가 양수면 그 값, 아니면 설정값(없으면 null)
     */
    public static Long resolveCredits(Long before, Long after, Integer configured) {
        if (before != null && after != null && before - after > 0) {
            return before - after;
        }
        return configured == null ? null : configured.longValue();
    }

    /**
     * @return 추정 비용(USD, 소수 6자리). 크레딧 수나 단가가 없거나 음수이면 null
     */
    public static BigDecimal estimate(Long credits, BigDecimal usdPerCredit) {
        if (credits == null || credits < 0 || usdPerCredit == null) {
            return null;
        }
        return usdPerCredit.multiply(BigDecimal.valueOf(credits)).setScale(6, RoundingMode.HALF_UP);
    }
}
