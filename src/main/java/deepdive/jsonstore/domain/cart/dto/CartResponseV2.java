package deepdive.jsonstore.domain.cart.dto;

import deepdive.jsonstore.domain.cart.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartResponseV2 {
    private Long id;
    private byte[] memberUid;
    private byte[] productUid;
    private Long amount;

    public CartResponseV2(Cart cart) {
        this.id = cart.getId();
        this.memberUid = cart.getMember().getUlid();
        this.productUid = cart.getProduct().getUlid();
        this.amount = cart.getAmount();
    }
}
