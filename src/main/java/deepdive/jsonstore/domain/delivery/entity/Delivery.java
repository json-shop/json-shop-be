package deepdive.jsonstore.domain.delivery.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private UUID uid;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT)
    )
    private Member member;

}
