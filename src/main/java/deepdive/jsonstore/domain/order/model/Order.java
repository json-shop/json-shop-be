package deepdive.jsonstore.domain.order.model;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.member.model.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column
    private BigDecimal total;

    @Column
    private String zipCode;

    @Column
    private String address;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @Column
    private String phone; // 수령인 번호

    @Column
    private String recipient; // 수령인

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> products= new ArrayList<>();

}
