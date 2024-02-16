package com.eumakase.eumakase.exception;

import com.eumakase.eumakase.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashSet;
import java.util.Set;

import static com.eumakase.eumakase.common.dto.ApiResponse.error;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 유효성 검사 실패 시 처리하는 메서드.
     * @Valid 어노테이션을 사용한 객체 검증 실패 시 발생하는 BindException 처리.
     */
    @ExceptionHandler(BindException.class) //
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected ApiResponse<?> handleValidationException(BindException ex) {
        log.warn("ValidationException({}) - {}", ex.getClass().getSimpleName(), ex.getMessage());

        // 필드 이름을 수집하는 Set
        Set<String> invalidFields = new HashSet<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            invalidFields.add(fieldError.getField());
        }

        // 메시지 생성
        String fields = String.join(", ", invalidFields);
        String message = fields + " 파라미터는 공백일 수 없습니다.";

        return error(message);
    }

    /**
     * 필수 요청 파라미터 누락 시 처리하는 메서드.
     * @RequestParam 어노테이션과 함께 필수 파라미터가 누락됐을 때 발생하는 ServletRequestBindingException 처리.
     */
    @ExceptionHandler(ServletRequestBindingException.class)// @RequestParam 누락
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleServletRequestBindingException(ServletRequestBindingException ex) {
        log.warn("{} - {}", ex.getClass().getName(), ex.getMessage());
        return error(ex.getMessage());
    }

    /**
     * 핸들러를 찾을 수 없는 경로에 대한 요청 처리.
     * 매핑되지 않은 경로로 요청이 들어왔을 때 발생하는 NoHandlerFoundException 처리.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("NoHandlerFoundException - {}", ex.getMessage());
        return error("요청한 경로를 찾을 수 없습니다.");
    }

    /**
     * 메서드 인자 타입 불일치 시 처리하는 메서드.
     * 경로 변수 또는 요청 파라미터의 타입이 일치하지 않을 때 발생하는 MethodArgumentTypeMismatchException 처리.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("MethodArgumentTypeMismatchException - {}", ex.getMessage());
        return error("잘못된 파라미터 형식: " + ex.getName() + "는 " + ex.getRequiredType().getSimpleName() + " 타입이어야 합니다.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        log.warn("MissingServletRequestParameterException - {}", ex.getMessage());
        String message = String.format("%s param값이 누락되었습니다.", ex.getParameterName());
        return error(message);
    }
}