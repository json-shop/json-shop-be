package deepdive.jsonstore.domain.member.dto;

import deepdive.jsonstore.domain.member.entity.Member;
import java.util.UUID;

public record MemberDto(
        UUID uuid,
        String username,
        String email,
        String phone,
        boolean isDeleted
) {
    public static MemberDto fromEntity(Member member) {
        return new MemberDto(
                member.getUid(),
                member.getUsername(),
                member.getEmail(),
                member.getPhone(),
                member.getIsDeleted()
        );
    }

    public Member toEntity(String encodedPassword) {
        return new Member(
                null, // ID는 자동 생성
                UUID.randomUUID(), // UUID 생성
                this.username,
                encodedPassword,
                this.email,
                this.phone,
                false, // isDeleted 초기값 설정
                null, // deletedAt 초기값 설정
                null //default delivery 초기값
        );
    }
}