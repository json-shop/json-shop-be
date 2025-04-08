package deepdive.jsonstore.common.advice;

import deepdive.jsonstore.common.dto.ErrorResponse;
import deepdive.jsonstore.common.exception.*;
import jakarta.persistence.EntityNotFoundException;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.JoinException;
import deepdive.jsonstore.common.exception.DeliveryException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.common.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.S3Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Spring valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionsHandler(MethodArgumentNotValidException ex) {
        // 첫 번째 에러만 꺼내서 CustomException으로 감쌈
        FieldError fieldError = ex.getBindingResult().getFieldError();

        String message = fieldError != null ? fieldError.getDefaultMessage() : "검증 오류입니다.";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(JsonStoreErrorCode.INVALID_INPUT_PARAMETER.name(), message));
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponse> commonExceptionHandler(CommonException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> orderExceptionHandler(OrderException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(JoinException.class)
    public ResponseEntity<ErrorResponse> joinExceptionHandler(JoinException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(DeliveryException.class)
    public ResponseEntity<ErrorResponse> deliveryExceptionHandler(DeliveryException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ErrorResponse> s3ExceptionHandler(S3Exception ex) {
        log.error(ex.awsErrorDetails().errorCode(), ex.awsErrorDetails().errorMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(JsonStoreErrorCode.S3_ERROR.getHttpStatus().name(),
                JsonStoreErrorCode.S3_ERROR.getMessage()));
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ErrorResponse> notificationExceptionHandler(NotificationException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJpaEntityNotFound(EntityNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                JsonStoreErrorCode.ENTITY_NOT_FOUND.name(),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, JsonStoreErrorCode.ENTITY_NOT_FOUND.getHttpStatus());
    }


}
