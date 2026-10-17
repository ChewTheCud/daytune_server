package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.admin.AdminLoginRequestDto;
import com.eumakase.eumakase.dto.admin.AdminLoginResponseDto;
import com.eumakase.eumakase.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 로그인 API (permitAll). 계약: docs/admin-api.md
 */
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponseDto>> login(@Valid @RequestBody AdminLoginRequestDto requestDto) {
        String accessToken = adminAuthService.login(requestDto.getEmail(), requestDto.getPassword());
        return ResponseEntity.ok(ApiResponse.success("관리자 로그인에 성공했습니다.", new AdminLoginResponseDto(accessToken)));
    }
}
