package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.auth.LoginRequestDto;
import com.eumakase.eumakase.dto.auth.LoginResponseDto;
import com.eumakase.eumakase.dto.auth.SignUpRequestDto;
import com.eumakase.eumakase.dto.auth.SignUpResponseDto;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.security.CustomUserDetailService;
import com.eumakase.eumakase.security.JwtIssuer;
import com.eumakase.eumakase.security.UserPrincipal;
import jakarta.security.auth.message.AuthException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtIssuer jwtIssuer;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Optional<User> findUser = userRepository.findByEmail(loginRequestDto.getEmail());
        if(findUser.isPresent()) {
            User user = findUser.get();
            user.updateLastLoginDate();
            String token = jwtIssue(user);

            return LoginResponseDto.of(user, token);
        } else {
            // TODO: 예외처리 구현
            return LoginResponseDto.builder()
                    .accessToken("failed")
                    .build();
        }
    }

    /**
     * User 생성
     */
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        try {

            // TODO: 24.01.20 이메일 중복 여부 판별

            // TODO: 24.01.20 닉네임 중복 여부 판별

            User user = signUpRequestDto.toEntity(signUpRequestDto, passwordEncoder);
            User savedUser = userRepository.save(user);

            return SignUpResponseDto.of(savedUser);
        } catch (Exception e) {
            // 예외 처리 로직
            throw new UserException(400, "User 생성 중 오류가 발생했습니다.");
        }
    }


    public String jwtIssue(User user){
        // 인증 객체 생성
        var authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getEmail()+"emokase"
        );

        // 사용자 인증 처리
        var authentication = authenticationManager.authenticate(authenticationToken);

        // 인증 정보를 Security Context에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 사용자의 세부 정보 가져오기
        var principal = (UserPrincipal) authentication.getPrincipal();

        // 사용자의 권한 목록 추출
        var roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // JWT 토큰 발급
        var token = jwtIssuer.issue(principal.getUserId(), principal.getEmail(), roles);

        return token;
    }
}