package com.eumakase.eumakase.dto.auth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 응답 데이터 내 kakaoAccount 키 값 내부
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class KakaoAccount {
    @JsonProperty("profile_nickname_needs_agreement")
    private Boolean profileNicknameNeedsAgreement;

    @JsonProperty("profile_image_needs_agreement")
    private Boolean profileImageNeedsAgreement;

    private Profile profile;

    @JsonProperty("email_needs_agreement")
    private Boolean emailNeedsAgreement;

    @JsonProperty("is_email_valid")
    private Boolean isEmailValid;

    @JsonProperty("is_email_verified")
    private Boolean isEmailVerified;

    private String email;
}