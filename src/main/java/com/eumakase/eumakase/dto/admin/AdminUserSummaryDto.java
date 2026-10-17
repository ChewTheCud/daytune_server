package com.eumakase.eumakase.dto.admin;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.util.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 유저 검색 결과 항목
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AdminUserSummaryDto {
    private Long id;
    private String nickname;
    private String email;
    private Role role;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedDate;

    public static AdminUserSummaryDto of(User user) {
        return new AdminUserSummaryDto(user.getId(), user.getNickname(), user.getEmail(), user.getRole(),
                user.getCreatedDate(), user.getLastLoginDate(), user.getDeletedDate());
    }
}
