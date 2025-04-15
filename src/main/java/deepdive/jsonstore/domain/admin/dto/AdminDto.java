package deepdive.jsonstore.domain.admin.dto;

import deepdive.jsonstore.domain.admin.entity.Admin;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminDto(
        UUID uid,
        String username,
        String email,
        String phone,
        boolean isDeleted,
        LocalDateTime deletedAt
) {

    // Admin → AdminDto
    public static AdminDto fromEntity(Admin admin) {
        return new AdminDto(
                admin.getUid(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getPhone(),
                admin.getDeleted(),
                admin.getDeletedAt()
        );
    }

    // AdminDto → Admin (비밀번호는 외부에서 인코딩된 값 전달)
    public Admin toEntity(String encodedPassword) {
        return Admin.builder()
                .uid(UUID.randomUUID())
                .username(this.username)
                .password(encodedPassword)
                .email(this.email)
                .phone(this.phone)
                .deleted(false)
                .deletedAt(null)
                .build();
    }
}
