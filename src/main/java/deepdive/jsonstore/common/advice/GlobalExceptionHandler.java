package deepdive.jsonstore.common.advice;

import deepdive.jsonstore.common.dto.ErrorExtraResponse;
import deepdive.jsonstore.common.dto.ErrorResponse;
import deepdive.jsonstore.common.exception.*;
import deepdive.jsonstore.domain.admin.exception.AdminException;
import deepdive.jsonstore.domain.cart.exception.CartException;
import deepdive.jsonstore.domain.notification.exception.NotificationException;
import deepdive.jsonstore.domain.order.exception.OrderException.OrderOutOfStockException;
import deepdive.jsonstore.domain.product.exception.ProductException;
import jakarta.persistence.EntityNotFoundException;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.JoinException;
import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.order.exception.OrderException;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), "서버 오류입니다."));
    }


    // Spring valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionsHandler(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
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
        ErrorResponse response;
        if (ex instanceof OrderOutOfStockException orderOutOfStockException) {
            response = new ErrorExtraResponse<>(
                    ex.getErrorCode().name(),
                    ex.getErrorCode().getMessage() + String.join(", ", orderOutOfStockException.getExtra()),
                    orderOutOfStockException.getExtra());
        } else {
            response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        }
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

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> cartExceptionHandler(CartException ex) {
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

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorResponse> ProductExceptionHandler(ProductException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(AdminException.class)
    public ResponseEntity<ErrorResponse> AdminExceptionHandler(AdminException ex) {
        log.info("AdminException: {}", ex.getErrorCode().name());
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }


    @ExceptionHandler(AuthException.AdminLoginFailedException.class)
    public ResponseEntity<ErrorResponse> adminLoginFailedExceptionHandler(AuthException.AdminLoginFailedException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> memberExceptionHandler(MemberException ex) {
        log.info("MemberException: {}", ex.getErrorCode().name());
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    // 인증 실패 (로그인 안 됨)
    @ExceptionHandler(AuthException.UnauthenticatedAccessException.class)
    public ResponseEntity<ErrorResponse> unauthenticatedAccessHandler(AuthException.UnauthenticatedAccessException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    // 인가 실패 (권한 부족)
    @ExceptionHandler(AuthException.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedHandler(AuthException.AccessDeniedException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    //접근 금지(다른 사용자 리소스 접근 시)
    @ExceptionHandler(AuthException.ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> forbiddenAccessHandler(AuthException.ForbiddenAccessException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode().name(), ex.getErrorCode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }


}
