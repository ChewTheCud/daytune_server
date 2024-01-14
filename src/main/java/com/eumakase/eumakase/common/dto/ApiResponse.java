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

    private ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    private ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(String status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "정상적으로 생성되었습니다.", data);
    }

    public static <T> ApiResponse<T> error(String status, String message) {
        return new ApiResponse<>(status, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", "에러가 발생했습니다");
    }
}
