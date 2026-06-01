package io.eddie.unitybe.common.exception;

import io.eddie.unitybe.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Validation 오류 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        String message = bindingResult.getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("잘못된 요청입니다.");

        return ResponseEntity.badRequest().body(ApiResponse.failure(message));
    }


    //비즈니스 로직 처리
    @ExceptionHandler(DiversionException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(DiversionException e) {
        ErrorCode eCode = e.getErrorCode();
        return ResponseEntity.status(eCode.getStatus()).body(ApiResponse.failure(eCode.getMessage()));
    }


}
