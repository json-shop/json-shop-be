package deepdive.jsonstore.domain.order.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(unique = true, columnDefinition = "CHAR(36)", nullable = false)
    private UUID uid;

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

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> products= new ArrayList<>();

    public void expire() {
        this.orderStatus = OrderStatus.EXPIRED;
    }
}
