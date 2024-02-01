package com.eumakase.eumakase.dto.auth;

import com.eumakase.eumakase.util.enums.Role;
import com.eumakase.eumakase.domain.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
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

    private String passwordSuffix;

    @NotBlank
    private String email;

    private String password;

    @NotBlank
    private String nickname;

    public User toEntity(final SignUpRequestDto signUpRequestDto, PasswordEncoder passwordEncoder, String passwordSuffix) {
        return User.builder()
                .email(signUpRequestDto.getEmail())
                // 비밀번호 암호화 (이메일+custom암호키)
                .password(passwordEncoder.encode(signUpRequestDto.getEmail()+passwordSuffix))
                .nickname(signUpRequestDto.getNickname())
                .role(Role.ROLE_USER)
                .build();
    }
}