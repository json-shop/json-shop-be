package deepdive.jsonstore.domain.cart.dto;

import deepdive.jsonstore.domain.cart.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartResponse {
    private Long id;
    private UUID memberUid;
    private UUID productUid;
    private Long amount;

    public CartResponse(Cart cart) {
        this.id = cart.getId();
        this.memberUid = cart.getMember().getUid();
        this.productUid = cart.getProduct().getUid();
        this.amount = cart.getAmount();
    }
}
