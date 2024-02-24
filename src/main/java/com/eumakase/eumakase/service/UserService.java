package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.user.UserReadResponseDto;
import com.eumakase.eumakase.exception.AuthException;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.repository.RefreshTokenRepository;
import com.eumakase.eumakase.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserReadResponseDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("해당하는 사용자를 찾을 수 없습니다."));

        return UserReadResponseDto.of(user);
    }

    public void updateUserNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("해당하는 사용자를 찾을 수 없습니다."));

        if (nickname.length() < 2 || nickname.length() > 10) {
            throw new AuthException("닉네임은 2~10글자 사이여야 합니다.");
        }

        user.setNickname(nickname);
        userRepository.save(user);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);

        // TODO: 연관된 다른 테이블에 대한 삭제 로직을 추가.

        // 사용자 정보를 조회하여 Soft Delete
        userRepository.findById(userId).ifPresent(user -> {
            user.setSnsId(null);
            user.setEmail(null);
            user.setPassword(null);
            user.setProfileImageUrl(null);
            user.setBirthDate(null);
            user.setPhoneNumber(null);
            user.setDeletedDate(LocalDateTime.now()); // 현재 시각으로 삭제 날짜 설정
            userRepository.save(user);
        });
    }

    public Optional<User> findBySnsId(String sndId) {
        return userRepository.findBySnsId(sndId);
    }


}