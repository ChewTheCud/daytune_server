package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.exception.AuthException;
import com.eumakase.eumakase.security.UserPrincipal;
import com.eumakase.eumakase.service.UserService;
import com.eumakase.eumakase.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Auth API
 */
@RestController
@RequestMapping(value = "/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Deactivate User
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> withdrawUser(@PathVariable Long userId, @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Long authenticatedUserId = currentUser.getId();
            SecurityUtil.verifyUserId(authenticatedUserId, userId);
            userService.withdrawUser(userId);
            return ResponseEntity.ok(ApiResponse.success("사용자 계정이 성공적으로 탈퇴되었습니다."));
        } catch (AuthException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("탈퇴 처리 중 문제가 발생했습니다."));
        }
    }
}