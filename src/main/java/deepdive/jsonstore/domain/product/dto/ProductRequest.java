package deepdive.jsonstore.domain.product.dto;

import java.util.UUID;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;

public record ProductRequest(
	String productName,
	String productDetail,
	Category category,
	int price,
	int stock
) {

	public Product toProduct(String url) {
		return Product.builder()
			.uuid(UUID.randomUUID())
			.name(productName)
			.category(category)
			.detail(productDetail)
			.price(price)
			.stock(stock)
			.image(url)
			.active(true)
			.build();
	}
}
