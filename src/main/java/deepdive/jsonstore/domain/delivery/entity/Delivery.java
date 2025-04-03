package deepdive.jsonstore.domain.delivery.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;

import java.util.UUID;

@Builder
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

//    @ManyToOne
//    @JoinColumn(nullable = false)
//    private Member member;
}
