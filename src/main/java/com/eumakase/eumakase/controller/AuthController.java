package com.eumakase.eumakase.controller;

import com.eumakase.eumakase.common.dto.ApiResponse;
import com.eumakase.eumakase.dto.auth.*;
import com.eumakase.eumakase.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Auth API
 */
@RestController
@RequestMapping(value = "/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Sign Up
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signup(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        try {
            SignUpResponseDto signUpResponseDto = authService.signUp(signUpRequestDto);
            return ResponseEntity.ok(ApiResponse.success("회원가입에 성공했습니다.",signUpResponseDto));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("회원가입에 실패했습니다."));
        }
    }

    /**
     * Login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody @Validated LoginRequestDto loginRequestDto){
        try {
            LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
            return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.",loginResponseDto));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("로그인에 실패했습니다."));
        }
    }

    /**
     * Access Token Reissue
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueAccessTokenResponseDto>> reissue(@RequestBody ReissueAccessTokenRequestDto reissueAccessTokenRequestDto) {
        try {
            ReissueAccessTokenResponseDto jwtReissueResponseDto = authService.reissue(reissueAccessTokenRequestDto.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("토큰 재발급에 성공했습니다.", jwtReissueResponseDto));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("토큰 재발급에 실패했습니다."));
        }
    }
}