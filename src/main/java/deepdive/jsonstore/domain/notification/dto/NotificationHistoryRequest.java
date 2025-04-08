package deepdive.jsonstore.domain.notification.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class NotificationHistoryRequest {

    @NotNull(message = "memberId를 입력해주세요.")
    @Positive(message = "memberId는 양수여야 합니다.")
    private Long memberId;
}
