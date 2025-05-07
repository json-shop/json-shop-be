package deepdive.jsonstore.domain.admin.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.common.util.UlidUtil;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Table(name = "admins")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID uid;

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private byte[] ulid;

    @Column(nullable = false, unique = true, length = 255)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String phone;

    @Column(nullable = false, name="isDeleted")
    private Boolean deleted;

    @Column
    private java.time.LocalDateTime deletedAt;

    /*
    // 관리자가 등록한 상품들 (1:N 관계)
    @OneToMany(mappedBy = "Admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;
*/
    // 엔티티가 저장되기 전에 UUID 자동 생성
    @PrePersist
    public void generateUUID() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID();
        }
        if (this.ulid == null) {
            this.ulid = UlidUtil.createUlidBytes();
        }
    }
}
