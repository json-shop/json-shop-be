package deepdive.jsonstore.domain.admin.dto;

import java.util.UUID;

import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;

public record CreateProductRequest(
	String productName,
	String productDetail,
	Category category,
	int price,
	int stock
) {

	public Product toProduct(String url, Admin admin) {
		return Product.builder()
			.uid(UUID.randomUUID())
			.admin(admin)
			.name(productName)
			.category(category)
			.detail(productDetail)
			.price(price)
			.stock(stock)
			.image(url)
			.status(ProductStatus.ON_SALE)
			.soldCount(0)
			.build();
	}
}
