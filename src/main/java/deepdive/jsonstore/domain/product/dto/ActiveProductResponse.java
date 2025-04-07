package deepdive.jsonstore.domain.product.dto;

import java.util.UUID;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import lombok.Builder;

@Builder
public record ActiveProductResponse(
	UUID uid,
	String productName,
	String image,
	String productDetail,
	Category category,
	int price,
	int stock
) {

	public static ActiveProductResponse toActiveProductResponse(Product product) {
		return ActiveProductResponse.builder()
			.uid(product.getUid())
			.productName(product.getName())
			.productDetail(product.getDetail())
			.image(product.getImage())
			.category(product.getCategory())
			.price(product.getPrice())
			.stock(product.getStock())
			.build();
	}
}
