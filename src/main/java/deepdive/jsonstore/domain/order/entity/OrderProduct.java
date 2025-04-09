package deepdive.jsonstore.domain.order.entity;

import deepdive.jsonstore.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Table(name = "order_product")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
//    @Column(unique = true, columnDefinition = "BINARY(16)", nullable = false)
    @Column(unique = true, columnDefinition = "BINARY(16)", nullable = false)
    private UUID uid = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "order_id",foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
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
