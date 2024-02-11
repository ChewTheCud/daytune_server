package com.eumakase.eumakase.dto.auth.kakao;

import com.eumakase.eumakase.dto.chatGPT.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("thumbnail_image_url")
    private String thumbnailImageUrl;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("is_default_image")
    private Boolean isDefaultImage;
}
