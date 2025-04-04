package deepdive.jsonstore.domain.product.dto;

import java.util.UUID;

import deepdive.jsonstore.domain.product.model.Category;
import lombok.Builder;

@Builder
public record ActiveProductResponse(
	UUID id,
	String productName,
	String image,
	String productDetail,
	Category category,
	int price,
	int stock
) {
}
