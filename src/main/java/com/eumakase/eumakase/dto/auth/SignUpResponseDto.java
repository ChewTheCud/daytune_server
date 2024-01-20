package com.eumakase.eumakase.dto.auth;

import com.eumakase.eumakase.domain.User;
import lombok.*;

import java.io.Serializable;

/**
 * Sign Up 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class SignUpResponseDto implements Serializable {
    private Long id;
    private String email;
    private String nickname;

    public static SignUpResponseDto of(User user) {
        return SignUpResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}