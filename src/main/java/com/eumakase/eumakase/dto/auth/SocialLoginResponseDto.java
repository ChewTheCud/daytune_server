package com.eumakase.eumakase.dto.auth;

import com.eumakase.eumakase.domain.User;
import lombok.*;

import java.io.Serializable;

/**
 * Social Login 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class SocialLoginResponseDto implements Serializable {
    private Long id;
    private String snsId;
    private String email;
    private String nickname;
    private String accessToken;
    private String refreshToken;

    public static SocialLoginResponseDto of(User user, String accessToken, String refreshToken) {
        return SocialLoginResponseDto.builder()
                .id(user.getId())
                .snsId(user.getSnsId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}