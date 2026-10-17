package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.admin.AdminCostsResponseDto;
import com.eumakase.eumakase.dto.admin.AdminSystemResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUsageDetailResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUsageResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUserDetailResponseDto;
import com.eumakase.eumakase.dto.admin.AdminUserPageResponseDto;
import com.eumakase.eumakase.service.AdminDashboardService;
import com.eumakase.eumakase.service.AdminUsageDetailService;
import com.eumakase.eumakase.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 관리자 대시보드 API (ROLE_ADMIN). 계약: docs/admin-api.md
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminDashboardService adminDashboardService;
    private final AdminUserService adminUserService;
    private final AdminUsageDetailService adminUsageDetailService;

    /**
     * 이용 현황
     */
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<AdminUsageResponseDto>> getUsage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("이용 현황 조회에 성공했습니다.", adminDashboardService.getUsage(from, to)));
    }

    /**
     * 이용 현황 KPI 상세 목록
     */
    @GetMapping("/usage/details")
    public ResponseEntity<ApiResponse<AdminUsageDetailResponseDto>> getUsageDetails(
            @RequestParam String metric,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("이용 현황 상세 조회에 성공했습니다.",
                adminUsageDetailService.getDetails(metric, from, to, page, size)));
    }

    /**
     * 외부 API 과금
     */
    @GetMapping("/costs")
    public ResponseEntity<ApiResponse<AdminCostsResponseDto>> getCosts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("외부 API 과금 조회에 성공했습니다.", adminDashboardService.getCosts(from, to)));
    }

    /**
     * 유저 검색
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<AdminUserPageResponseDto>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("유저 검색에 성공했습니다.", adminUserService.search(query, page, size)));
    }

    /**
     * 유저 상세
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponseDto>> getUserDetail(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("유저 상세 조회에 성공했습니다.", adminUserService.getDetail(userId, from, to)));
    }

    /**
     * 시스템 상태
     */
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<AdminSystemResponseDto>> getSystem(@RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(ApiResponse.success("시스템 상태 조회에 성공했습니다.", adminDashboardService.getSystem(hours)));
    }
}
