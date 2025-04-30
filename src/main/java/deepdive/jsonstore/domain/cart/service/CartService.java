package deepdive.jsonstore.domain.cart.service;

import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.exception.CartException;
import deepdive.jsonstore.domain.cart.repository.CartRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.product.entity.Product;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartValidateService validateService;
    private final MeterRegistry meterRegistry;

    // 카트에 상품 추가
    public Cart addProductToCart(UUID memberUid, UUID productUid, Long amount) {
        Member member = validateService.validateMember(memberUid);
        log.info("member = {}", member.getUid());
        Product product = validateService.validateProduct(productUid, amount);

        // 이미 있는 상품을 등록하려는 경우
        Cart cart = alreadyInCart(member, product, amount);
        if (cart != null) {
            return cart;
        }

        // 새 상품인데 수량이 0 이하인 경우
        validateService.validateNewCartAmount(amount);

        meterRegistry.counter("business.cart.added").increment();

        Cart newCart = Cart.builder()
                .member(member)
                .product(product)
                .amount(amount)
                .build();
        return cartRepository.save(newCart);
    }


    // 카트에 상품 추가
    public Cart addProductToCart(byte[] memberUid, byte[] productUid, Long amount) {
        Member member = validateService.validateMember(memberUid);
        log.info("member={}", Base64.getUrlEncoder().encodeToString(member.getUlid()));

        Product product = validateService.validateProduct(productUid, amount);
        log.info("product={}", Base64.getUrlEncoder().encodeToString(product.getUlid()));

        // 이미 있는 상품을 등록하려는 경우
        Cart cart = alreadyInCart(member, product, amount);
        if (cart != null) {
            return cart;
        }

        // 새 상품인데 수량이 0 이하인 경우
        validateService.validateNewCartAmount(amount);

        meterRegistry.counter("business.cart.added").increment();

        Cart newCart = Cart.builder()
                .member(member)
                .product(product)
                .amount(amount)
                .build();
        return cartRepository.save(newCart);
    }


    // 카트에 상품이 존재할 경우 수량 체크 후 수량추가
    public Cart alreadyInCart(Member member, Product product, Long amount) {
        return cartRepository.findByMemberAndProduct(member, product)
                .map(cart -> {
                    Long newAmount = validateService.validateAmount(cart, product, amount);
                    cart.setAmount(newAmount);
                    return cartRepository.save(cart);
                })
                .orElse(null); // 등록된 게 없으면 null 반환 (신규 장바구니 생성)
    }

    // 카트 상품 목록 제거
    public void deleteCartByCartId(Long cartId) {
        // 카트 목록이 있는지 조회
        validateService.validateCart(cartId);

        meterRegistry.counter("business.cart.deleted").increment();
        // 카트 제거
        cartRepository.deleteById(cartId);
    }

    // 카트 리스트 조회
    public List<Cart> getCartByMemberUid(UUID memberUid) {
        // 멤버ID 기반으로 카트 리스트 조회
        List<Cart> carts = cartRepository.findByMember_Uid(memberUid);

        return carts;
    }

    // 카트 리스트 조회
    public Page<Cart> getCartByMemberUid(byte[] memberUid, Pageable pageable) {
        // 멤버ID 기반으로 카트 리스트 조회
        Page<Cart> carts = cartRepository.findByMember_Ulid(memberUid, pageable);

        return carts;
    }
}
