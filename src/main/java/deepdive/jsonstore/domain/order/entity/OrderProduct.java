package deepdive.jsonstore.domain.order.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(name = "order_products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class OrderProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(unique = true, columnDefinition = "BINARY(16)", nullable = false)
    private UUID uid = UUID.randomUUID();

    @Builder.Default
    @Column(unique = true, columnDefinition = "BINARY(16)", nullable = false)
    private byte[] ulid = UlidUtil.createUlidBytes();

    @Setter
    @ManyToOne
    @JoinColumn(name = "order_id",foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id",foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    private int price; // 실결제 금액
    private int quantity;

    public static OrderProduct from(Product product, int quantity) {
        return OrderProduct.builder()
                .product(product)
                .price(product.getPrice())
                .quantity(quantity)
                .build();
    }
}
