package deepdive.jsonstore.domain.product.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductException extends RuntimeException {

	private final ProductErrorCode errorCode;

	public static class ProductNotFoundException extends ProductException {
		public ProductNotFoundException() {
			super(ProductErrorCode.PRODUCT_NOT_FOUND);
		}
	}

	public static class ProductForbiddenException extends ProductException {
		public ProductForbiddenException() {
			super(ProductErrorCode.PRODUCT_FORBIDDEN);
		}
	}

}
