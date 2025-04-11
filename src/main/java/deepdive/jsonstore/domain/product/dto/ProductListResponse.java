package deepdive.jsonstore.domain.product.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.ProductStatus;

public record ProductListResponse(
	UUID uid,
	String productName,
	String image,
	Category category,
	int price,
	ProductStatus status,
	LocalDateTime updatedAt
) {
}
