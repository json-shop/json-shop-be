package deepdive.jsonstore.domain.member.dto;

public record ResetPasswordRequestDTO(
        String currentPassword,
        String newPassword,
        String newPasswordConfirm
) {
}
