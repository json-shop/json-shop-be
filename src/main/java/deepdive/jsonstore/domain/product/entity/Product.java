package deepdive.jsonstore.domain.product.entity;

import java.util.UUID;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.admin.dto.UpdateProductRequest;
import deepdive.jsonstore.domain.admin.entity.Admin;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	private UUID uid;
	private String name;
	private int stock;
	private int price;
	private String image;
	private String detail;
	@Enumerated(EnumType.STRING)
	private Category category;
	private ProductStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "admin_id",
		foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
	)
	private Admin admin;

	public void updateProduct(UpdateProductRequest updateProductRequest, String image) {
		this.name = updateProductRequest.productName();
		this.stock = updateProductRequest.stock();
		this.price = updateProductRequest.price();
		this.image = image;
		this.detail = updateProductRequest.productDetail();
		this.category = updateProductRequest.category();
		this.status = updateProductRequest.status();
	}
}
