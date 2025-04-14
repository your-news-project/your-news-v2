package kr.co.yournews.common.response.exception;

import kr.co.yournews.common.response.error.ErrorResponse;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import kr.co.yournews.common.response.error.type.GlobalErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* CustomException 예외 처리 */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<?> handleCustomException(final CustomException e) {
        BaseErrorType error = e.getErrorType();
        log.error("[Error Occurred] {}", error.getMessage());
        return ResponseEntity.status(error.getStatus().getCode()).body(ErrorResponse.from(error));
    }

    /* Argument Validation 예외 처리 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleValidationException(final MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.error("[Error Occurred] {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .code(GlobalErrorType.VALIDATION_ERROR.getCode())
                .errors(errors)
                .build());
    }

    /* 일반 예외 처리 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(final Exception e) {
        log.error("[Error Occurred] {}", e.getMessage());
        BaseErrorType error = GlobalErrorType.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(error.getStatus().getCode()).body(ErrorResponse.from(error));
    }
}
