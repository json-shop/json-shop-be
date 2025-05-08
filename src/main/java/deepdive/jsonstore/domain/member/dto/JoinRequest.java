package deepdive.jsonstore.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record JoinRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "유효한 이메일을 입력해주세요.")
        @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 9, message = "비밀번호는 9자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        String confirmPassword,

        @NotBlank(message = "이름을 입력해주세요.")
        String username,

        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{9,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        String phone
) {
}
