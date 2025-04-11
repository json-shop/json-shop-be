package deepdive.jsonstore.domain.cart.controller;

import deepdive.jsonstore.domain.cart.dto.CartDeleteRequest;
import deepdive.jsonstore.domain.cart.dto.CartRequest;
import deepdive.jsonstore.domain.cart.dto.CartResponse;
import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartApiController {
    private final CartService cartService;

    // 카트에 상품 추가
    @PostMapping
    public ResponseEntity<CartResponse> addProductToCart(@Valid @RequestBody CartRequest request) {
        Cart cart = cartService.addProductToCart(request.getMemberId(), request.getProductId(), request.getAmount());
        return ResponseEntity.ok(new CartResponse(cart));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCartByMemberId(@Valid CartDeleteRequest request) {
        cartService.deleteCartByCartId(request.getCartId());
        return ResponseEntity.noContent().build();
    }
}
