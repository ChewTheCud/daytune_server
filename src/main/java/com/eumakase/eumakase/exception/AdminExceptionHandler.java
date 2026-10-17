package com.eumakase.eumakase.exception;

import com.eumakase.eumakase.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * AdminException → 지정 상태 코드 + ApiResponse.error
 */
@RestControllerAdvice
@Slf4j
public class AdminExceptionHandler {

    @ExceptionHandler(AdminException.class)
    public ResponseEntity<ApiResponse<Void>> handleAdminException(AdminException ex) {
        log.warn("AdminException({}) - {}", ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getMessage()));
    }
}
