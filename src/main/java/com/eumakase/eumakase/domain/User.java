package com.eumakase.eumakase.domain;

import com.eumakase.eumakase.common.domain.BaseEntity;
import com.eumakase.eumakase.util.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String snsId;

    @Column
    private String email;

    @JsonIgnore
    @Column
    private String password;

    @Column
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    public User(Optional<User> findUser) {
        super();
    }

    public void updateLastLoginDate() {
        this.lastLoginDate = LocalDateTime.now();
    }
}