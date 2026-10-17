package com.eumakase.eumakase.support;

/**
 * 테스트 전용 관리자 자격 증명. 운영·로컬 관리자 비밀번호와 무관하다 (실제 비밀번호를 테스트에 두지 않는다)
 * HASH는 Spring BCryptPasswordEncoder($2y$, cost 4)로 PASSWORD를 해시한 값이다
 */
public final class TestAdminCredentials {
    public static final String PASSWORD = "test-only-admin-pw";
    public static final String HASH = "$2y$04$Ia55aZYkPE2ZD2UzZIEaPuI8G9bhDT/GuvbvndmDgzJEGpLu1lz0a";

    private TestAdminCredentials() {
    }
}
