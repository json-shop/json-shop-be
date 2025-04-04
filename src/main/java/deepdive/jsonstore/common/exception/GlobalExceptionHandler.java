package deepdive.jsonstore.common.exception;

import deepdive.jsonstore.common.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<? extends ErrorDto> defaultHandler(CustomException e) {
        var errorDto = new ErrorDto(e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .body(errorDto);
    }
    // @Valid 검증 실패 예외 처리 추가
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        var errorDto = new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorDto);
    }


}
