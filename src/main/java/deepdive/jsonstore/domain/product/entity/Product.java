package deepdive.jsonstore.domain.product.entity;

import java.util.UUID;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.product.dto.ActiveProductResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
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
	@Enumerated(EnumType.STRING)
	private Category category;
	@Column(name = "isActive")
	private boolean active;

	//TODO 관리자 추가할 예정

}
