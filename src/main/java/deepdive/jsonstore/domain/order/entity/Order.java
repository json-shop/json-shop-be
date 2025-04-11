package deepdive.jsonstore.domain.order.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Table(name = "orders")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(unique = true, columnDefinition = "BINARY(16)", nullable = false)
    private UUID uid = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column
    private int total;

    @Column
    private String zipCode;

    @Column
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private OrderStatus orderStatus;

    @Column(updatable = false)
    private LocalDateTime expiredAt;

    @Column
    private String phone; // 수령인 번호

    @Column
    private String recipient; // 수령인

    @Column
    @Setter
    private String paymentKey; // 토스페이먼츠 주문키

    @Builder.Default
    @Column
    private String currency = "KRW"; // 통화

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> products = new ArrayList<>();

    public void addProduct(OrderProduct orderProduct) {
        products.add(orderProduct);
        orderProduct.setOrder(this);
    }

    public void expire() {
        this.orderStatus = OrderStatus.EXPIRED;
    }

    public void changeState(OrderStatus status) {
        this.orderStatus = status;
    }

    public boolean isAnyOutOfStock() {
        return this.products.stream()
                .anyMatch(p -> p.getProduct().getStock() < p.getQuantity());
    }

    // TODO : N + 1?
    public String getTitle() {
        if (products == null || products.isEmpty())
            return "";
        String firstName = products.getFirst().getProduct().getName();
        int rest = products.size() - 1;
        return rest > 0 ? firstName + " 외 " + rest + "개" : firstName;
    }
}
