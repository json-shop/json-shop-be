package deepdive.jsonstore.domain.cart.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CartException extends RuntimeException {

	private final CartErrorCode errorCode;

	public static class ProductNotFoundException extends CartException {
		public ProductNotFoundException() {
			super(CartErrorCode.PRODUCT_NOT_FOUND);
		}
	}

	public static class ProductForbiddenException extends CartException {
		public ProductForbiddenException() {
			super(CartErrorCode.PRODUCT_FORBIDDEN);
		}
	}

	public static class ProductOutOfStockException extends CartException {
		public ProductOutOfStockException() {
			super(CartErrorCode.PRODUCT_OUT_OF_STOCK);
		}
	}

	public static class CartNotFoundException extends CartException {
		public CartNotFoundException() {
			super(CartErrorCode.CART_NOT_FOUND);
		}
	}

	public static class InvalidAmountException extends CartException {
		public InvalidAmountException() {
			super(CartErrorCode.INVALID_AMOUNT);
		}
	}
  
	public static class MemberNotFoundException extends CartException {
		public MemberNotFoundException() {
			super(CartErrorCode.MEMBER_NOT_FOUND);
		}
	}
}
