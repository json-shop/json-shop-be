package deepdive.jsonstore.domain.product.dto;

import deepdive.jsonstore.domain.product.entity.Product;

public record ProductOrderCountDTO(
	Product product,
	long count
) {
}
