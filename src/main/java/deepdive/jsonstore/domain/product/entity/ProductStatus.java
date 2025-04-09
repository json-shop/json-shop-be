package deepdive.jsonstore.domain.product.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ProductStatus {
	ON_SALE("판매중"), TEMPORARY_SOLD_OUT("일시품절"), SOLD_OUT("품절"), DISCONTINUED("판매중지");

	private final String name;
}
