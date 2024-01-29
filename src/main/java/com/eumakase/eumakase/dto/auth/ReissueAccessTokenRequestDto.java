package com.eumakase.eumakase.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * JWT 재발급 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class ReissueAccessTokenRequestDto implements Serializable {
    @NotBlank
    private String refreshToken;
}