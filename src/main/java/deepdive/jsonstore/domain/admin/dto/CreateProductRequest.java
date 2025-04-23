package deepdive.jsonstore.domain.admin.dto;

import java.util.UUID;

import de.huxhorn.sulky.ulid.ULID;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import lombok.Builder;

@Builder
public record CreateProductRequest(
        String productName,
        String productDetail,
        Category category,
        int price,
        int stock
) {

    public Product toProduct(String url, Admin admin, byte[] imageByte) {
        return Product.builder()
                .uid(UUID.randomUUID())
                .ulid(UlidUtil.createUlidBytes())
                .admin(admin)
                .name(productName)
                .category(category)
                .detail(productDetail)
                .price(price)
                .stock(stock)
                .image(url)
                .status(ProductStatus.ON_SALE)
                .soldCount(0)
                .imageByte(imageByte)
                .build();
    }
}
