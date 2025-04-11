package deepdive.jsonstore.domain.cart.service;

import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.exception.CartException;
import deepdive.jsonstore.domain.cart.repository.CartRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartValidateService validateService;

    // 카트에 상품 추가
    public Cart addProductToCart(Long memberId, Long productId, Long amount) {
        Member member = validateService.validateMember(memberId);
        Product product = validateService.validateProduct(productId, amount);

        // 이미 있는 상품을 등록하려는 경우
        Cart cart = alreadyInCart(member, product, amount);
        if (cart != null) {
            return cart;
        }

        // 새 상품인데 수량이 0 이하인 경우
        validateService.validateNewCartAmount(amount);

        Cart newCart = Cart.builder()
                .member(member)
                .product(product)
                .amount(amount)
                .build();
        return cartRepository.save(newCart);
    }


    // 카트에 상품이 존재할 경우 수량 체크 후 수량추가
    public Cart alreadyInCart(Member member, Product product, Long amount) {
        Cart cart = cartRepository.findByMemberAndProduct(member, product);

        if (cart != null) {
            amount = validateService.validateAmount(cart, product, amount);
            cart.setAmount(amount);
            return cartRepository.save(cart);
        }
        return null;
    }

    // 카트 리스트 조회
    public List<Cart> getCartByMemberId(Long memberId) {
        // 멤버ID 기반으로 카트 리스트 조회
        List<Cart> carts = cartRepository.findByMemberId(memberId);

        // 카트 리스트가 비었는지 확인
        validateService.validateCartList(carts);

        return carts;
    }
}
