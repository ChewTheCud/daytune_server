package com.eumakase.eumakase.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * Social Login 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class SocialLoginRequestDto implements Serializable {

    @NotBlank
    private String socialType;

    @NotBlank
    private String oauthAccessToken;

    private String nickname;

    private String fcmToken;
}