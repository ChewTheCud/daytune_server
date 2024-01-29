package com.eumakase.eumakase.dto.auth;

import com.eumakase.eumakase.domain.User;
import lombok.*;

import java.io.Serializable;

/**
 * Login 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class LoginResponseDto implements Serializable {
    private Long id;
    private String email;
    private String nickname;
    private String accessToken;
    private String refreshToken;

    public static LoginResponseDto of(User user, String accessToken, String refreshToken) {
        return LoginResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}