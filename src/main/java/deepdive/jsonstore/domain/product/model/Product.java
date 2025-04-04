package deepdive.jsonstore.domain.product.model;

import java.util.UUID;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.product.dto.ActiveProductResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private UUID uuid;
	private String name;
	private int stock;
	private int price;
	private String image;
	private String detail;
	private Category category;
	private boolean isActive;

	//TODO 관리자 추가할 예정

	public ActiveProductResponse toActiveProductResponse() {
		return ActiveProductResponse.builder()
			.id(uuid)
			.productName(name)
			.productDetail(detail)
			.image(image)
			.category(category)
			.price(price)
			.stock(stock)
			.build();
	}

}
