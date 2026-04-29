package com.secondHand.SecondHandMarket.global.exception;

import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import jakarta.persistence.OptimisticLockException;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

// @RestControllerAdvice: 모든 @RestController에서 발생하는 예외를 한 곳에서 처리
// @ControllerAdvice + @ResponseBody 조합
// 이점:
//   각 컨트롤러/서비스에서 try-catch 반복 제거
//   예외별 응답 형식 통일 (ApiResponse 포맷 일관성 보장)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException: 비즈니스 로직 예외 (직접 정의한 예외)
    // ErrorCode에 정의된 HttpStatus를 그대로 응답 상태코드로 사용
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }

    // MethodArgumentNotValidException: @Valid 검증 실패 시 발생
    // 예: @NotBlank, @Size, @Email 등 Bean Validation 위반
    // 여러 필드 오류가 있을 때 첫 번째 오류 메시지만 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("입력값 오류입니다");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(Exception e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(ErrorCode.PRODUCT_CONFLICT.getMessage()));
    }

}
