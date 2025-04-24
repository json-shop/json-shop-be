package deepdive.jsonstore.domain.admin.dto;

import java.util.UUID;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import lombok.Builder;

@Builder
public record UpdateProductRequest(
        UUID uid,
        String ulid,
        String productName,
        String productDetail,
        Category category,
        int price,
        int stock,
        ProductStatus status
) {
}

