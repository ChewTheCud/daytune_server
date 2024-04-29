package com.eumakase.eumakase.dto.auth.apple;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 애플 유저 정보 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Data
public class AppleUserInfoResponseDto {
    // 고유ID
    @JsonProperty("sub")
    private String subject;

    @JsonProperty("email")
    private String email;

}