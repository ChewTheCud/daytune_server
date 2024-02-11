package com.eumakase.eumakase.dto.auth;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.util.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;



/**
 * Social Sign Up 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class SocialSignUpRequestDto implements Serializable {
    @NotBlank
    private String snsId;

    private String email;

    private String password;

    private String nickname;

    private String profileImageUrl;



    public User toEntity(String snsId, String email, String nickname, String profileImageUrl, PasswordEncoder passwordEncoder, String passwordSuffix) {
        return User.builder()
                .snsId(snsId)
                .email(email)
                // 비밀번호 암호화 (email+custom암호키)
                .password(passwordEncoder.encode(email+passwordSuffix))
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .role(Role.ROLE_USER)
                .build();
    }
}