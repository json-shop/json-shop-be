package deepdive.jsonstore.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotNull(message = "회원 ID를 입력해주세요.")
    @Positive(message = "회원 ID는 0보다 커야 합니다.")
    private Long memberId;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 255, message = "제목은 최대 255자까지 입력 가능합니다.")
    private String title;

    @NotBlank(message = "메시지를 입력해주세요.")
    @Size(max = 255, message = "메시지는 최대 255자까지 입력 가능합니다.")
    private String message;
}
