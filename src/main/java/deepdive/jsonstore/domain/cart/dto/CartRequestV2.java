package deepdive.jsonstore.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartRequestV2 {
    @NotNull(message = "productUid를 입력해주세요.")
    private byte[] productUid;

    @NotNull(message = "수량을 입력해주세요.")
    private Long amount;
}
