package com.eumakase.eumakase.dto.user;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.User;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User 조회 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class UserReadResponseDto implements Serializable {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private String birthDate;

    public static UserReadResponseDto of(User user) {
        return UserReadResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .birthDate(user.getBirthDate())
                .build();
    }
}