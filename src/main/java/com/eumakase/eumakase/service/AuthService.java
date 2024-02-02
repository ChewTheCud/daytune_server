package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.RefreshToken;
import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.dto.auth.*;
import com.eumakase.eumakase.dto.auth.kakao.KakaoResponseDto;
import com.eumakase.eumakase.exception.AuthException;
import com.eumakase.eumakase.exception.UserException;
import com.eumakase.eumakase.repository.RefreshTokenRepository;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.security.JwtDecoder;
import com.eumakase.eumakase.security.JwtIssuer;
import com.eumakase.eumakase.security.UserPrincipal;;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${security.jwt.password-suffix}")
    private String passwordSuffix;

    // JWT 발급 및 검증을 위한 JwtIssuer 객체
    private final JwtIssuer jwtIssuer;

    // 사용자 정보를 저장하고 관리하는 UserRepository 객체
    private final UserRepository userRepository;

    // Spring Security의 인증 관리자
    private final AuthenticationManager authenticationManager;

    // 리프레시 토큰을 관리하는 RefreshTokenRepository 객체
    private final RefreshTokenRepository refreshTokenRepository;

    // 비밀번호 암호화를 위한 PasswordEncoder 객체
    private final PasswordEncoder passwordEncoder;

    // JWT 토큰을 디코딩하기 위한 JwtDecoder 객체
    private final JwtDecoder jwtDecoder;

    // 소셜 로그인 관련 서비스를 제공하는 SocialService 객체
    private final SocialService socialService;

    /**
     * 일반 로그인 처리 메서드.
     * 이메일을 사용하여 사용자를 찾고 JWT 토큰을 발급함.
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new AuthException("User not found with email: " + loginRequestDto.getEmail()));

        return createLoginResponse(user);
    }

    /**
     * 소셜 로그인 처리 메서드.
     * 소셜 로그인 타입에 따라 사용자 프로필 정보를 가져온 후 JWT 토큰 발급.
     */
    public SocialLoginResponseDto socialLogin(SocialLoginRequestDto socialLoginRequestDto) {
        String socialType = socialLoginRequestDto.getSocialType();
        String oauthAccessToken = socialLoginRequestDto.getOauthAccessToken();

        // if(socialType.equals("kakao")) {
            KakaoResponseDto kakaoResponseDto = socialService.getKakaoUserProfile(oauthAccessToken);
            String snsId = kakaoResponseDto.getId();
            String email = kakaoResponseDto.getKakaoAccount().getEmail();
            String nickname = kakaoResponseDto.getKakaoAccount().getProfile().getNickname();

            User user = userRepository.findBySnsId(snsId)
                    .orElseGet(() -> createUserFromSocialData(snsId, email, nickname));

            return createSocialLoginResponse(user);
        // }

       // TODO: Apple Oauth2 로그인 로직 추가
    }

    /**
     * 사용자 회원가입 처리 메서드.
     * SignUpRequestDto를 받아 새로운 User 객체를 생성하고 저장함.
     */
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        try {
            // TODO: 24.01.20 이메일 중복 여부 판별

            // TODO: 24.01.20 닉네임 중복 여부 판별

            User user = signUpRequestDto.toEntity(signUpRequestDto, passwordEncoder, passwordSuffix);
            userRepository.save(user);

            return SignUpResponseDto.of(user);
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

    /**
     * 사용자 정보를 바탕으로 JWT 액세스 토큰을 발급하는 메서드.
     */
    public String jwtIssue(User user){

        // 인증 객체 생성
        var authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getEmail()+passwordSuffix
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

    // 새로운 사용자를 생성하는 Helper 메서드
    private User createUserFromSocialData(String snsId, String email, String nickname) {

        SocialSignUpRequestDto socialSignUpRequestDto = null;
        User user = socialSignUpRequestDto.toEntity(snsId, email, nickname, passwordEncoder, passwordSuffix);

        return userRepository.save(user);
    }

    // 일반 로그인 응답을 생성하는 Helper 메서드
    private LoginResponseDto createLoginResponse(User user) {
        String accessToken = jwtIssue(user);
        String refreshToken = jwtIssuer.issueRefreshToken(user.getId(), user.getEmail());
        manageRefreshToken(user, refreshToken);
        return LoginResponseDto.of(user, accessToken, refreshToken);
    }

    // 소셜 로그인 응답을 생성하는 Helper 메서드
    private SocialLoginResponseDto createSocialLoginResponse(User user) {
        String accessToken = jwtIssue(user);
        String refreshToken = jwtIssuer.issueRefreshToken(user.getId(), user.getEmail());
        manageRefreshToken(user, refreshToken);
        return SocialLoginResponseDto.of(user, accessToken, refreshToken);
    }

    // 리프레시 토큰을 관리하는 Helper 메서드
    private void manageRefreshToken(User user, String refreshToken) {
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);
        refreshTokenRepository.save(new RefreshToken(user, refreshToken));
    }
}
