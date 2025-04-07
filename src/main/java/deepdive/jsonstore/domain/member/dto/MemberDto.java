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
}
