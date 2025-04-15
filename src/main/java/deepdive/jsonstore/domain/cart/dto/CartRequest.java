package deepdive.jsonstore.domain.cart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartRequest {
    @NotNull(message = "productUid를 입력해주세요.")
    private UUID productUid;

    @NotNull(message = "수량을 입력해주세요.")
    private Long amount;
}
