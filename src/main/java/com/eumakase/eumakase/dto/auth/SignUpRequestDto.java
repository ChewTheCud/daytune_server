package com.eumakase.eumakase.dto.auth;

import com.eumakase.eumakase.domain.Role;
import com.eumakase.eumakase.domain.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * Sign Up 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class SignUpRequestDto implements Serializable {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String nickname;

    public User toEntity(final SignUpRequestDto signUpRequestDto) {
        return User.builder()
                .email(signUpRequestDto.getEmail())
                .password(signUpRequestDto.getPassword())
                .nickname(signUpRequestDto.getNickname())
                .build();
    }
}