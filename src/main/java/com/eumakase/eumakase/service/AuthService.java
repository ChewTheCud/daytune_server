package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.auth.SignUpRequestDto;
import com.eumakase.eumakase.dto.auth.SignUpResponseDto;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * User 생성
     */
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        try {

            // TODO: 24.01.20 이메일 중복 여부 판별

            // TODO: 24.01.20 닉네임 중복 여부 판별

            User user = signUpRequestDto.toEntity(signUpRequestDto);
            User savedUser = userRepository.save(user);

            return SignUpResponseDto.of(savedUser);
        } catch (Exception e) {
            // 예외 처리 로직
            throw new UserException(400, "User 생성 중 오류가 발생했습니다.");
        }
    }
}