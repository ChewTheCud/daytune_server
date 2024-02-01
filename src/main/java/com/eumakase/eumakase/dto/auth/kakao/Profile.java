package com.eumakase.eumakase.dto.auth.kakao;

import com.eumakase.eumakase.dto.chatGPT.Message;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 응답 데이터 내 profile 키 값 내부
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class Profile {
    private String nickname;
    private String thumbnailImageUrl;
    private String profileImageUrl;
    private Boolean isDefaultImage;
}
