package deepdive.jsonstore.domain.cart.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode {

	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
	CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니을 찾을 수 없습니다."),
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
	PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN, "상품에 접근할 수 없습니다."),
	PRODUCT_OUT_OF_STOCK(HttpStatus.CONFLICT, "상품의 재고가 부족합니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
