package deepdive.jsonstore.domain.cart.controller;

import deepdive.jsonstore.domain.cart.dto.CartDeleteRequest;
import deepdive.jsonstore.domain.cart.dto.CartListRequest;
import deepdive.jsonstore.domain.cart.dto.CartRequest;
import deepdive.jsonstore.domain.cart.dto.CartResponse;
import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartApiController {
    private final CartService cartService;

    // 장바구니에 상품 추가
    @PostMapping
    public ResponseEntity<CartResponse> addProductToCart(@Valid @RequestBody CartRequest request) {
        Cart cart = cartService.addProductToCart(request.getMemberUid(), request.getProductUid(), request.getAmount());
        return ResponseEntity.ok(new CartResponse(cart));
    }

    // 장바구니 상품 삭제
    @DeleteMapping
    public ResponseEntity<?> deleteCartByMemberId(@Valid @RequestBody CartDeleteRequest request) {
        cartService.deleteCartByCartId(request.getCartId());
        return ResponseEntity.noContent().build();
    }

    // 특정 멤버 카트 상품 조회
    @GetMapping
    public ResponseEntity<List<CartResponse>> getCartByMemberId(@Valid CartListRequest request) {
        List<Cart> cart = cartService.getCartByMemberId(request.getMemberUid());
        List<CartResponse> response = cart.stream()
                .map(CartResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
