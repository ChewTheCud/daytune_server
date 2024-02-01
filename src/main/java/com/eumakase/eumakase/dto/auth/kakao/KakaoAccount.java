package com.eumakase.eumakase.dto.auth.kakao;

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
    private Boolean profileNeedsAgreement;
    private Profile profile;
    private Boolean emailNeedsAgreement;
    private Boolean isEmailValid;
    private Boolean isEmailVerified;
    private String email;
}