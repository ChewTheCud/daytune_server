package com.eumakase.eumakase.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 관리자 로그인 요청 DTO
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class AdminLoginRequestDto {
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    // 비밀번호가 로그에 찍히지 않도록 toString에서 뺀다
    @Override
    public String toString() {
        return "AdminLoginRequestDto(email=" + email + ")";
    }
}
