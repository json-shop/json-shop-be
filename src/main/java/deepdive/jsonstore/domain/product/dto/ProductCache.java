package deepdive.jsonstore.domain.product.dto;

import deepdive.jsonstore.domain.product.entity.ProductDocument;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductCache(
        List<ProductResponse> content,
        long totalElements
) {
}
