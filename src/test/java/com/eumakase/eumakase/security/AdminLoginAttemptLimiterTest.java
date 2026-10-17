package com.eumakase.eumakase.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AdminLoginAttemptLimiterTest {

    // 테스트에서 시간을 옮길 수 있는 시계
    private static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-18T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final AdminLoginAttemptLimiter limiter = new AdminLoginAttemptLimiter(clock);

    private void fail(int times, String email) {
        for (int i = 0; i < times; i++) {
            limiter.recordFailure(email);
        }
    }

    @Test
    void 실패_4회까지는_잠기지_않고_5회째_잠긴다() {
        fail(4, "a@x.com");
        assertThat(limiter.isLocked("a@x.com")).isFalse();
        fail(1, "a@x.com");
        assertThat(limiter.isLocked("a@x.com")).isTrue();
    }

    @Test
    void 이메일_대소문자와_앞뒤_공백은_같은_키다() {
        fail(5, " Admin@X.com ");
        assertThat(limiter.isLocked("admin@x.com")).isTrue();
    }

    @Test
    void 잠금은_15분_뒤_풀리고_카운터도_초기화된다() {
        fail(5, "a@x.com");
        clock.advance(Duration.ofMinutes(15).minusSeconds(1));
        assertThat(limiter.isLocked("a@x.com")).isTrue();
        clock.advance(Duration.ofSeconds(1));
        assertThat(limiter.isLocked("a@x.com")).isFalse();
        fail(1, "a@x.com");
        assertThat(limiter.isLocked("a@x.com")).isFalse();
    }

    @Test
    void 창_15분이_지나면_실패_횟수가_초기화된다() {
        fail(4, "a@x.com");
        clock.advance(Duration.ofMinutes(15));
        fail(4, "a@x.com");
        assertThat(limiter.isLocked("a@x.com")).isFalse();
    }

    @Test
    void 성공하면_카운터가_초기화된다() {
        fail(4, "a@x.com");
        limiter.recordSuccess("a@x.com");
        fail(4, "a@x.com");
        assertThat(limiter.isLocked("a@x.com")).isFalse();
    }

    @Test
    void 다른_이메일은_서로_영향이_없다() {
        fail(5, "a@x.com");
        assertThat(limiter.isLocked("b@x.com")).isFalse();
    }
}
