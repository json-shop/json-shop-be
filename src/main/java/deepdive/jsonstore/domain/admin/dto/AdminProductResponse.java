package deepdive.jsonstore.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import lombok.Builder;

@Builder
public record AdminProductResponse(
	UUID uid,
	String productName,
	String image,
	String productDetail,
	Category category,
	int price,
	int stock,
	ProductStatus status,
	long soldCount,
	LocalDateTime createdAt
) {

	public static AdminProductResponse toAdminProductResponse(Product product) {
		return AdminProductResponse.builder()
			.uid(product.getUid())
			.productName(product.getName())
			.productDetail(product.getDetail())
			.image(product.getImage())
			.category(product.getCategory())
			.price(product.getPrice())
			.stock(product.getStock())
			.status(product.getStatus())
			.soldCount(product.getSoldCount())
			.createdAt(product.getCreatedAt())
			.build();
	}
}
