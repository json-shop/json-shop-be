package deepdive.jsonstore.domain.cart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartRequest {
    @NotNull(message = "memberId를 입력해주세요.")
    @Positive(message = "memberId는 0보다 커야 합니다.")
    private Long memberId;

    @NotNull(message = "productId를 입력해주세요.")
    @Positive(message = "productId는 0보다 커야 합니다.")
    private Long productId;

    @NotNull(message = "수량을 입력해주세요.")
    private Long amount;
}
