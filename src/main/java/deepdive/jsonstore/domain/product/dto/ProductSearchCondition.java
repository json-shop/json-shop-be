package deepdive.jsonstore.domain.product.dto;

import deepdive.jsonstore.domain.product.entity.Category;

public record ProductSearchCondition(
	Category category,
	ProductSortType sort,
	String search
) {
}
