package deepdive.jsonstore.domain.admin.dto;

import java.util.UUID;

import deepdive.jsonstore.domain.product.entity.Category;

public record UpdateProductRequest(
	UUID uid,
	String productName,
	String productDetail,
	Category category,
	int price,
	int stock,
	String image
) {
}
