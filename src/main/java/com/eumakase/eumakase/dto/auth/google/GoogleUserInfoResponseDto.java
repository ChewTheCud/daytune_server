package com.eumakase.eumakase.dto.auth.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 구글 유저 정보 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Data
public class GoogleUserInfoResponseDto {
    // 고유 ID
    @JsonProperty("sub")
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @JsonProperty("picture")
    private String picture;
}