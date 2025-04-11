package deepdive.jsonstore.domain.cart.service;

import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.exception.CartErrorCode;
import deepdive.jsonstore.domain.cart.exception.CartException;
import deepdive.jsonstore.domain.cart.repository.CartRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartValidateService {
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    // 멤버 검증
    public Member validateMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(CartException.MemberNotFoundException::new);
    }

    // 상품 검증
    public Product validateProduct(Long productId, Long amount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(CartException.ProductNotFoundException::new);

        // 상품이 판매중인지 검증
        if (!product.getStatus().equals(ProductStatus.ON_SALE))
            throw new CartException.ProductForbiddenException();

        // 상품 수량이 적절한지 검증
        if (product.getStock() < amount)
            throw new CartException.ProductOutOfStockException();

        return product;
    }

    public Long validateAmount(Cart cart, Product product, Long amount) {
        long sumAmount = cart.getAmount() + amount;
        if (product.getStock() < sumAmount)
            throw new CartException.ProductOutOfStockException();
        return sumAmount;
    }

    // 장바구니가 있는지 조회
    public void validateCart(Long cartId) {
        cartRepository.findById(cartId)
                .orElseThrow(CartException.CartNotFoundException::new);
    }
}
