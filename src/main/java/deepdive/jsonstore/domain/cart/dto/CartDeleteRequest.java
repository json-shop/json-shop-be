package deepdive.jsonstore.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDeleteRequest {
    @NotNull(message = "cartId를 입력해주세요.")
    @Positive(message = "cartId는 양수여야 합니다.")
    private Long cartId;
}
