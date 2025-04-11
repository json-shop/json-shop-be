package deepdive.jsonstore.domain.cart.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode {

	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
	CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 내역을 찾을 수 없습니다."),
	INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "장바구니 내역의 수량을 정상적으로 입력해주세요."),
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
	PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN, "상품에 접근할 수 없습니다."),
	PRODUCT_OUT_OF_STOCK(HttpStatus.CONFLICT, "상품의 재고가 부족합니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
