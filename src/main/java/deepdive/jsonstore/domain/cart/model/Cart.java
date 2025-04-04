package deepdive.jsonstore.domain.cart.model;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.member.model.Member;
import deepdive.jsonstore.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)  // 한 회원당 하나의 장바구니
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long amount;
}
