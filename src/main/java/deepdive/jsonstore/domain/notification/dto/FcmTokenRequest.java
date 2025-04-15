package deepdive.jsonstore.domain.notification.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {
    @NotBlank(message = "FCM 토큰이 없습니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9:_\\-+=/]+$",
            message = "FCM 토큰 형식이 올바르지 않습니다."
    )
    private String token;
}
