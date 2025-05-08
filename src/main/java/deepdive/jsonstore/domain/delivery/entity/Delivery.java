package deepdive.jsonstore.domain.delivery.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private UUID uid;

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private byte[] ulid;

    @Setter
    @Column(nullable = false)
    private String address;

    @Setter
    @Column(nullable = false)
    private String zipCode;

    @Setter
    @Column(nullable = false)
    private String phone;

    @Setter
    @Column(nullable = false)
    private String recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Member member;

    public void updateDelivery(DeliveryRegRequestDTO dto) {
        this.address = dto.address();
        this.zipCode = dto.zipCode();
        this.phone = dto.phone();
        this.recipient = dto.recipient();
    }

}
