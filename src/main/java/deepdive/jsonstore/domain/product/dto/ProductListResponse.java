package deepdive.jsonstore.domain.product.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import lombok.Builder;

@Builder
public record ProductListResponse(
	UUID uid,
	String productName,
	String image,
	Category category,
	int price,
	ProductStatus status,
	LocalDateTime createdAt
) {
	public static ProductListResponse toProductListResponse(Product product) {
		return ProductListResponse.builder()
			.uid(product.getUid())
			.productName(product.getName())
			.category(product.getCategory())
			.price(product.getPrice())
			.status(product.getStatus())
			.build();
	}
}
