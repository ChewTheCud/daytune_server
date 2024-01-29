package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.RefreshToken;
import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.auth.*;
import com.eumakase.eumakase.exception.AuthException;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.repository.RefreshTokenRepository;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.security.CustomUserDetailService;
import com.eumakase.eumakase.security.JwtDecoder;
import com.eumakase.eumakase.security.JwtIssuer;
import com.eumakase.eumakase.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtIssuer jwtIssuer;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService customUserDetailService;
    private final JwtDecoder jwtDecoder;

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

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
     */
    public ReissueAccessTokenResponseDto reissue(String refreshToken) {
        // 리프레시 토큰 검증
        if (!jwtIssuer.validateRefreshToken(refreshToken)) {
            throw new AuthException("Invalid refresh token");
        }

        // 리프레시 토큰으로부터 사용자 정보 추출
        String email = jwtDecoder.extractEmailFromRefreshToken(refreshToken);
        System.out.println("email:"+email);
        // 해당 이메일의 사용자가 존재하는지 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(400, "User not found"));

        // 리프레시 토큰이 해당 사용자에게 속하는지 확인
        refreshTokenRepository.findByUser(user)
                .filter(rt -> rt.getRefreshToken().equals(refreshToken))
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        // 새로운 액세스 토큰 발급
        String newAccessToken =  jwtIssuer.issue(user.getId(), user.getEmail(), Collections.singletonList(user.getRole().toString()));
        return ReissueAccessTokenResponseDto.of(newAccessToken);
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
