package deepdive.jsonstore.domain.admin.dto;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AdminProductListResponseWithSafeUlid(
    UUID uid,
    String ulid,
    String productName,
    String image,
    Category category,
    int price,
    int stock,
    ProductStatus status,
    long soldCount,
    LocalDateTime createdAt
){

}
