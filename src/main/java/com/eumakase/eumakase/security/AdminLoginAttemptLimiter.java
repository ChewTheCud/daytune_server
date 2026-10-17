package com.eumakase.eumakase.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 관리자 로그인 실패 제한: 15분 창 내 5회 실패 → 15분 잠금. 성공하면 초기화.
 * - 키는 소문자 이메일 (존재하지 않는 이메일도 같은 규칙)
 * - 인메모리다. 단일 EC2 인스턴스 배포 전제이며, 재시작하면 초기화된다.
 *   다중 인스턴스가 되면 Redis 등 공유 저장소로 옮겨야 한다.
 */
@Component
public class AdminLoginAttemptLimiter {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    // 임의 이메일로 계속 실패시켜도 메모리가 무한히 늘지 않도록 이 크기를 넘으면 만료 항목을 정리한다
    private static final int PURGE_THRESHOLD = 10_000;

    private final Clock clock;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Autowired
    public AdminLoginAttemptLimiter() {
        this(Clock.systemDefaultZone());
    }

    AdminLoginAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean isLocked(String email) {
        Attempt attempt = attempts.get(key(email));
        return attempt != null && attempt.lockedUntil != null && clock.instant().isBefore(attempt.lockedUntil);
    }

    public void recordFailure(String email) {
        Instant now = clock.instant();
        if (attempts.size() > PURGE_THRESHOLD) {
            attempts.values().removeIf(attempt -> attempt.isExpired(now));
        }
        attempts.compute(key(email), (k, attempt) -> {
            if (attempt == null || attempt.isExpired(now)) {
                attempt = new Attempt(now);
            }
            attempt.failures++;
            if (attempt.failures >= MAX_FAILURES) {
                attempt.lockedUntil = now.plus(LOCK_DURATION);
            }
            return attempt;
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(key(email));
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Attempt {
        private final Instant windowStart;
        private volatile int failures;
        private volatile Instant lockedUntil;

        private Attempt(Instant windowStart) {
            this.windowStart = windowStart;
        }

        // 잠금 중이면 잠금 해제 시각까지, 아니면 창(15분)이 지나면 만료
        private boolean isExpired(Instant now) {
            if (lockedUntil != null) {
                return !now.isBefore(lockedUntil);
            }
            return !now.isBefore(windowStart.plus(WINDOW));
        }
    }
}
