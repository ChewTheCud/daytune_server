package com.eumakase.eumakase.dto.auth;

import com.eumakase.eumakase.domain.Role;
import com.eumakase.eumakase.domain.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private String password;

    @NotBlank
    private String nickname;

    public User toEntity(final SignUpRequestDto signUpRequestDto, PasswordEncoder passwordEncoder) {
        return User.builder()
                .email(signUpRequestDto.getEmail())
                // 비밀번호 암호화 (이메일+custom암호키)
                .password(passwordEncoder.encode(signUpRequestDto.getEmail()+"emokase"))
                .nickname(signUpRequestDto.getNickname())
                .role(Role.ROLE_USER)
                .build();
    }
}