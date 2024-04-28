package com.eumakase.eumakase.dto.auth.apple;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 애플 ID 토큰 응답 정보 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class AppleResponseDto {
    @JsonProperty("sub")
    private String subject;

    @JsonProperty("email")
    private String email;

}