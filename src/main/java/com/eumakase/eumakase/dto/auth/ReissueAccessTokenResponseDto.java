package com.eumakase.eumakase.dto.auth;

import lombok.*;

/**
 * JWT 재발급 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class ReissueAccessTokenResponseDto {
    private String accessToken;

    public static ReissueAccessTokenResponseDto of(String newAccessToken) {
        return ReissueAccessTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .build();
    }
}