package deepdive.jsonstore.domain.product.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode {

	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
	PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 상품에 접근할 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
