package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
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