package deepdive.jsonstore.domain.notification.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class NotificationHistoryRequest {

    @NotNull(message = "회원 UUID를 입력해주세요.")
    private UUID memberUid;
}
