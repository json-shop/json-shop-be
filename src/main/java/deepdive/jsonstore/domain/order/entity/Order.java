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
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
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
    private List<OrderProduct> orderProducts = new ArrayList<>();

    public void addOrderProduct(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
        orderProduct.setOrder(this);
    }

    public void expire() {
        this.orderStatus = OrderStatus.EXPIRED;
    }

    public void changeState(OrderStatus status) {
        this.orderStatus = status;
    }

    // TODO : 반정규화?, 수정시 바뀌어야함
    // service로 옮기기?
    // n + 1
    public String getTitle() {
        if (orderProducts == null || orderProducts.isEmpty())
            return "";
        String firstName = orderProducts.getFirst().getProduct().getName();
        int rest = orderProducts.size() - 1;
        return rest > 0 ? firstName + " 외 " + rest + "개" : firstName;
    }

    public void updateDelivery(String address, String zipCode, String phone, String recipient) {
        this.address = address;
        this.zipCode = zipCode;
        this.phone = phone;
        this.recipient = recipient;
    }
}
