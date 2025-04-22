package deepdive.jsonstore.domain.product.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import lombok.Builder;

@Builder
public record ProductResponse(
        UUID uid,
        UUID ulid,
        String productName,
        String image,
        String productDetail,
        Category category,
        int price,
        int stock,
        ProductStatus status,
        String adminName,
        LocalDateTime createdAt
) {

    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .uid(product.getUid())
                .ulid(product.getUlid())
                .productName(product.getName())
                .productDetail(product.getDetail())
                .image(product.getImage())
                .category(product.getCategory())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .adminName(product.getAdmin().getUsername())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
