package com.eumakase.eumakase.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    // 성공 응답을 위한 생성자
    private ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 오류 응답을 위한 생성자
    private ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // 성공 응답 메서드
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("SUCCESS", message);
    }

    // 오류 응답 메서드
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message);
    }
}