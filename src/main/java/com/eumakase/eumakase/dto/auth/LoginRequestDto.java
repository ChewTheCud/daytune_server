package com.eumakase.eumakase.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * Login 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class LoginRequestDto implements Serializable {

    @NotBlank
    private String email;

    private String password;
}