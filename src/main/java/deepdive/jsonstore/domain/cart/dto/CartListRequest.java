package deepdive.jsonstore.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartListRequest {
    @NotNull(message = "memberUid를 입력해주세요.")
    private UUID memberUid;
}
