package deepdive.jsonstore.domain.cart.service;

import deepdive.jsonstore.domain.cart.dto.CartRequest;
import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.exception.CartException;
import deepdive.jsonstore.domain.cart.repository.CartRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        Cart cart = alreadyInCart(member, product, amount);
        if (cart != null) {
            return cart;
        }

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

}
