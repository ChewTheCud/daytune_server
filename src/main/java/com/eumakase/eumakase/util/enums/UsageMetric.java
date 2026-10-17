package com.eumakase.eumakase.util.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * GET /admin/usage/details 의 metric 파라미터
 */
@Getter
@RequiredArgsConstructor
public enum UsageMetric {
    SIGNUPS("signups"),
    ACTIVE_USERS("activeUsers"),
    DIARIES("diaries"),
    MUSICS("musics"),
    SHARES("shares"),
    PUSHES("pushes");

    private final String value;

    public static Optional<UsageMetric> fromValue(String value) {
        return Arrays.stream(values()).filter(metric -> metric.value.equals(value)).findFirst();
    }
}
