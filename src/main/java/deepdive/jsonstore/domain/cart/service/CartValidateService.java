package deepdive.jsonstore.domain.cart.service;

import deepdive.jsonstore.domain.cart.entity.Cart;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartValidateService {
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    // 멤버 검증
    public Member validateMember(UUID memberUid) {
        return memberRepository.findByUid(memberUid)
                .orElseThrow(CartException.MemberNotFoundException::new);
    }

    // 멤버 검증
    public Member validateMember(byte[] memberUid) {
        return memberRepository.findByUlid(memberUid)
                .orElseThrow(CartException.MemberNotFoundException::new);
    }

    // 상품 검증
    public Product validateProduct(UUID productUid, Long amount) {
        Product product = productRepository.findByUid(productUid)
                .orElseThrow(CartException.ProductNotFoundException::new);

        return product;
    }


    // 상품 검증
    public Product validateProduct(byte[] productUid, Long amount) {
        Product product = productRepository.findByUlid(productUid)
                .orElseThrow(CartException.ProductNotFoundException::new);

        return product;
    }

    public Long validateAmount(Cart cart, Product product, Long amount) {
        long sumAmount = cart.getAmount() + amount;
        if (product.getStock() < sumAmount)
            throw new CartException.ProductOutOfStockException();
        if (sumAmount < 1 || amount == 0)
            throw new CartException.InvalidAmountException();
        return sumAmount;
    }

    // 장바구니가 있는지 조회
    public void validateCart(Long cartId) {
        cartRepository.findById(cartId)
                .orElseThrow(CartException.CartNotFoundException::new);
    }
  
    public void validateNewCartAmount(Long amount) {
        if (amount < 1) {
            throw new CartException.InvalidAmountException();
        }
    }

    public void validateCartList(List<Cart> carts) {
        if (carts.isEmpty()) {
            throw new CartException.CartNotFoundException();
        }
    }
}
