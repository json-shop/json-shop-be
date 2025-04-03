package deepdive.jsonstore.domain.delivery.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
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
    private UUID uuid;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String zipcode;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

}