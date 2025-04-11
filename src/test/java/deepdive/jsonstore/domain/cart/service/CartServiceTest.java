package deepdive.jsonstore.domain.cart.service;

import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.repository.CartRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartValidateService validateService;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("addProductToCart 테스트")
    class AddProductToCart {

        @Test
        @DisplayName("성공 - 카트에 상품이 없을 때")
        void success_whenNotInCart() {
            // given
            Long memberId = 1L;
            Long productId = 100L;
            Long amount = 2L;

            Member member = Member.builder().id(memberId).build();
            Product product = Product.builder().id(productId).build();
            Cart newCart = Cart.builder().member(member).product(product).amount(amount).build();

            when(validateService.validateMember(memberId)).thenReturn(member);
            when(validateService.validateProduct(productId, amount)).thenReturn(product);
            when(cartRepository.findByMemberAndProduct(member, product)).thenReturn(null);
            when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

            // when
            Cart result = cartService.addProductToCart(memberId, productId, amount);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(amount);
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("성공 - 카트에 동일 상품이 존재할 때 수량 갱신")
        void success_whenAlreadyInCart() {
            // given
            Long memberId = 1L;
            Long productId = 100L;
            Long currentAmount = 1L;
            Long addedAmount = 2L;
            Long updatedAmount = 3L;

            Member member = Member.builder().id(memberId).build();
            Product product = Product.builder().id(productId).build();
            Cart existingCart = Cart.builder().member(member).product(product).amount(currentAmount).build();

            when(validateService.validateMember(memberId)).thenReturn(member);
            when(validateService.validateProduct(productId, addedAmount)).thenReturn(product);
            when(cartRepository.findByMemberAndProduct(member, product)).thenReturn(existingCart);
            when(validateService.validateAmount(existingCart, product, addedAmount)).thenReturn(updatedAmount);
            when(cartRepository.save(existingCart)).thenReturn(existingCart);

            // when
            Cart result = cartService.addProductToCart(memberId, productId, addedAmount);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(updatedAmount);
            verify(cartRepository).save(existingCart);
        }
    }

    @Nested
    @DisplayName("alreadyInCart 테스트")
    class AlreadyInCart {

        @Test
        @DisplayName("성공 - 카트에 존재하는 상품이 있을 때 수량 갱신")
        void success_whenExists() {
            // given
            Member member = Member.builder().id(1L).build();
            Product product = Product.builder().id(100L).build();
            Cart cart = Cart.builder().member(member).product(product).amount(1L).build();
            Long newAmount = 5L;

            when(cartRepository.findByMemberAndProduct(member, product)).thenReturn(cart);
            when(validateService.validateAmount(cart, product, newAmount)).thenReturn(newAmount);
            when(cartRepository.save(cart)).thenReturn(cart);

            // when
            Cart result = cartService.alreadyInCart(member, product, newAmount);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(newAmount);
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("성공 - 카트에 존재하지 않을 때 null 반환")
        void success_whenNotExists() {
            // given
            Member member = Member.builder().id(1L).build();
            Product product = Product.builder().id(100L).build();
            Long amount = 2L;

            when(cartRepository.findByMemberAndProduct(member, product)).thenReturn(null);

            // when
            Cart result = cartService.alreadyInCart(member, product, amount);

            // then
            assertThat(result).isNull();
            verify(cartRepository, never()).save(any());
        }
    }
}
