package com.eumakase.eumakase.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * GET /admin/users 응답 DTO (유저 검색, 페이지)
 */
@Getter
@AllArgsConstructor
public class AdminUserPageResponseDto {
    private List<AdminUserSummaryDto> content;
    private int page;
    private int size;
    private long totalElements;
}
