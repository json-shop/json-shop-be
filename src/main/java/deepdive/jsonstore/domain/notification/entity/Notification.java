package deepdive.jsonstore.domain.notification.entity;

import deepdive.jsonstore.common.entity.BaseEntity;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ulid", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private byte[] ulid;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Member member;

    public void generateUlid() {
        this.ulid = UlidUtil.createUlidBytes();
    }
}
