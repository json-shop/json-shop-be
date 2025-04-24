package deepdive.jsonstore.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import lombok.Builder;

@Builder
public record AdminProductListResponse(
	UUID uid,
	byte[] ulid,
	String productName,
	String image,
	Category category,
	int price,
	int stock,
	ProductStatus status,
	long soldCount,
	LocalDateTime createdAt
) {
}
