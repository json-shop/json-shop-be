package deepdive.jsonstore.domain.order.model;

import deepdive.jsonstore.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(unique = true, columnDefinition = "BINARY(16)", nullable = false)
    @Column(unique = true, columnDefinition = "CHAR(36)", nullable = false)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column
    private Long price; // 실결제 금액

    @Column
    private Integer quantity;



}
