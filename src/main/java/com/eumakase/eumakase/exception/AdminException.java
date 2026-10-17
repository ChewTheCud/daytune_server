package com.eumakase.eumakase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 관리자 API 예외. 메시지는 그대로 응답에 나가므로 고정 문구만 넣는다 (외부 오류 원문 금지)
 */
@Getter
public class AdminException extends RuntimeException {
    private final HttpStatus status;

    public AdminException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
