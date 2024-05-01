package com.eumakase.eumakase.util;

import com.eumakase.eumakase.exception.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    /**
     * 현재 인증된 사용자의 ID와 주어진 사용자 ID가 일치하는지 검증
     * @param authenticatedUserId 현재 인증된 사용자의 ID
     * @param userId 검증하고자 하는 대상 사용자의 ID
     * @return true if the IDs match, otherwise false
     */
    public static void verifyUserId(Long authenticatedUserId, Long userId) {
        if (!authenticatedUserId.equals(userId)) {
            throw new AuthException(403, "접근 권한이 없습니다.");
        }
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        return null; // 사용자명을 찾을 수 없는 경우
    }
}
