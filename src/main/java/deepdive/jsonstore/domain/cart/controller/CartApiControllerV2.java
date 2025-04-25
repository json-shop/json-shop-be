package deepdive.jsonstore.domain.cart.controller;

import de.huxhorn.sulky.ulid.ULID;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.cart.dto.*;
import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/carts")
public class CartApiControllerV2 {
    private final CartService cartService;

    // 장바구니에 상품 추가
    @PostMapping
    public ResponseEntity<CartResponseV2> addProductToCart(
            @AuthenticationPrincipal(expression = "ulid") byte[] memberUlid,
            @Valid @RequestBody CartRequestV2 request) {
        log.info("member={}", Base64.getUrlEncoder().encodeToString(memberUlid));

        Cart cart = cartService.addProductToCart(memberUlid, request.getProductUid(), request.getAmount());
        return ResponseEntity.ok(new CartResponseV2(cart));
    }

    // 장바구니 상품 삭제
    @DeleteMapping
    public ResponseEntity<?> deleteCartByMemberId(@Valid @RequestBody CartDeleteRequest request) {
        cartService.deleteCartByCartId(request.getCartId());
        return ResponseEntity.noContent().build();
    }

    // 특정 멤버 카트 상품 조회
    @GetMapping
    public ResponseEntity<Page<CartResponseV2>> getCartByMemberId(@AuthenticationPrincipal(expression = "ulid") byte[] memberUlid, Pageable pageable) {
        log.info("member={}", Base64.getEncoder().encodeToString(memberUlid));

        Page<Cart> cart = cartService.getCartByMemberUid(memberUlid, pageable);
        Page<CartResponseV2> response = cart.map(CartResponseV2::new);

        return ResponseEntity.ok(response);
    }
}
