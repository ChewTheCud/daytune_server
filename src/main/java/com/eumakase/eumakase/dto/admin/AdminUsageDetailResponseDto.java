package com.eumakase.eumakase.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /admin/usage/details 응답 DTO (KPI 상세 목록, 페이지)
 */
@Getter
@AllArgsConstructor
public class AdminUsageDetailResponseDto {
    private List<Item> content;
    private int page;
    private int size;
    private long totalElements;

    @Getter
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private Long userId;
        private String nickname;
        private LocalDateTime createdDate;
        private String detail; // 일기 본문·가사는 넣지 않는다
    }
}
