package deepdive.jsonstore.domain.cart.dto;

import deepdive.jsonstore.domain.cart.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartResponse {
    private Long id;
    private Long memberId;
    private Long productId;
    private Long amount;

    public CartResponse(Cart cart) {
        this.id = cart.getId();
        this.memberId = cart.getMember().getId();
        this.productId = cart.getProduct().getId();
        this.amount = cart.getAmount();
    }
}
