package com.eumakase.eumakase.dto.auth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * 카카오 유저 정보 조회 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class KakaoUserInfoResponseDto implements Serializable {
    private String id;
    private Map<String, String> properties;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;
}