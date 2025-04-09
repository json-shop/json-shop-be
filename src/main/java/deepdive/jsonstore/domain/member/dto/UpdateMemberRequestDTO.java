package deepdive.jsonstore.domain.member.dto;

public record UpdateMemberRequestDTO(
        String username,
        String phone
) {
}
