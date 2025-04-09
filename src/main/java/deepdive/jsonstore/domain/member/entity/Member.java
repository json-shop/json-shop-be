package deepdive.jsonstore.domain.member.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.common.exception.MemberException;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID uid;

    @Column(nullable = false, unique = true, length = 255)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String phone;

    @Column(nullable = false)
    private Boolean isDeleted; // 회원 가입 시 기본값 false

    @Column
    private LocalDateTime deletedAt; // 삭제 시점 (삭제될 때만 값이 들어감)

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_delivery_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT) )
    private Delivery defaultDelivery; // 기본 배송지

//   // 회원 - 배송지
//   @OneToMany(mappedBy = "Member", cascade = CascadeType.ALL, orphanRemoval = true)
//   private List<Delivery> delivery;
    /*
   // 회원 - 장바구니
   @OneToMany(mappedBy = "Member", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<Cart> cart;

    // 회원 - 주문
   @OneToMany(mappedBy = "Member", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<Order> order;

   // 회원 - 알람
   @OneToMany(mappedBy = "Member", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<Notification> notification;
*/


    // UUID 자동 생성 로직
    @PrePersist
    public void prePersist() {
        this.uid = (this.uid == null) ? UUID.randomUUID() : this.uid;
    }

    // 회원 삭제 처리 메서드
    public void deleteMember() {
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new MemberException.AlreadyDeletedException();
        }

        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void resetPassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }
}