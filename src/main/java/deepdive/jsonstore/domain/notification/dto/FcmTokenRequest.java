package deepdive.jsonstore.domain.notification.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {
    @NotNull(message = "회원 ID를 입력해주세요.")
    @Positive(message = "회원 ID는 0보다 커야 합니다.")
    private Long memberId;

    @NotBlank(message = "FCM 토큰이 없습니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9:_\\-+=/]+$",
            message = "FCM 토큰 형식이 올바르지 않습니다."
    )
    private String token;
}