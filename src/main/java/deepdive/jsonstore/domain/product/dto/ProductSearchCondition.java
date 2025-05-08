package deepdive.jsonstore.domain.product.dto;

import deepdive.jsonstore.domain.product.entity.Category;
import lombok.Builder;

@Builder
public record ProductSearchCondition(
	Category category,
	ProductSortType sort,
	String search
) {
	public boolean isEmpty() {
		return this.category == null && this.sort == null && this.search == null;
	}
}
