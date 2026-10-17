package com.eumakase.eumakase.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자 로그인 응답 DTO
 */
@Getter
@AllArgsConstructor
public class AdminLoginResponseDto {
    private String accessToken;
}
