package deepdive.jsonstore.common.exception;

import deepdive.jsonstore.common.dto.ErrorDto;
import org.springframework.http.ResponseEntity;
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

}
