package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.user.UserReadResponseDto;
import com.eumakase.eumakase.exception.AuthException;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.security.UserPrincipal;
import com.eumakase.eumakase.service.UserService;
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
     * 사용자 정보 조회 (단일)
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<UserReadResponseDto>> getUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Long authenticatedUserId = currentUser.getId();
            UserReadResponseDto userReadResponseDto = userService.getUser(authenticatedUserId);
            return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회에 성공했습니다.",userReadResponseDto));
        } catch (UserException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 정보 조회에 실패했습니다."));
        }
    }


    /**
     * Update User Nickname
     */
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<Void>> updateNickname(@RequestParam String nickname,
                                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Long authenticatedUserId = currentUser.getId();

            userService.updateUserNickname(authenticatedUserId, nickname);

            // 성공 응답 반환
            return ResponseEntity.ok(ApiResponse.success("닉네임이 성공적으로 수정되었습니다."));
        } catch (AuthException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (UserException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("닉네임 수정 중 문제가 발생했습니다."));
        }
    }

    /**
     * 사용자 탈퇴
     */
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<Void>> withdrawUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Long authenticatedUserId = currentUser.getId();
            userService.withdrawUser(authenticatedUserId);
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

//    /**
//     * 사용자 강제 탈퇴
//     */
//    @DeleteMapping("/{userId}")
//    public ResponseEntity<ApiResponse<Void>> withdrawUser(@PathVariable Long userId, @AuthenticationPrincipal UserPrincipal currentUser) {
//        try {
//            Long authenticatedUserId = currentUser.getId();
//            SecurityUtil.verifyUserId(authenticatedUserId, userId);
//            userService.withdrawUser(userId);
//            return ResponseEntity.ok(ApiResponse.success("사용자 계정이 성공적으로 탈퇴되었습니다."));
//        } catch (AuthException e) {
//            return ResponseEntity
//                    .status(e.getStatusCode())
//                    .body(ApiResponse.error(e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(ApiResponse.error("탈퇴 처리 중 문제가 발생했습니다."));
//        }
//    }
}