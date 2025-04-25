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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
            UUID memberUid = UUID.randomUUID();
            UUID productUid = UUID.randomUUID();
            Long amount = 2L;

            Member member = Member.builder().uid(memberUid).build();
            Product product = Product.builder().uid(productUid).build();
            Cart newCart = Cart.builder().member(member).product(product).amount(amount).build();

            when(validateService.validateMember(memberUid)).thenReturn(member);
            when(validateService.validateProduct(productUid, amount)).thenReturn(product);
            when(cartRepository.findByMemberAndProduct(member, product)).thenReturn(null);
            when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

            // when
            Cart result = cartService.addProductToCart(memberUid, productUid, amount);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(amount);
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("성공 - 카트에 동일 상품이 존재할 때 수량 갱신")
        void success_whenAlreadyInCart() {
            // given
            UUID memberUid = UUID.randomUUID();
            UUID productUid = UUID.randomUUID();
            Long currentAmount = 1L;
            Long addedAmount = 2L;
            Long updatedAmount = 3L;

            Member member = Member.builder().uid(memberUid).build();
            Product product = Product.builder().uid(productUid).build();
            Cart existingCart = Cart.builder().member(member).product(product).amount(currentAmount).build();

            when(validateService.validateMember(memberUid)).thenReturn(member);
            when(validateService.validateProduct(productUid, addedAmount)).thenReturn(product);
            when(cartRepository.findByMemberAndProduct(member, product).get()).thenReturn(existingCart);
            when(validateService.validateAmount(existingCart, product, addedAmount)).thenReturn(updatedAmount);
            when(cartRepository.save(existingCart)).thenReturn(existingCart);

            // when
            Cart result = cartService.addProductToCart(memberUid, productUid, addedAmount);

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
            Member member = Member.builder().uid(UUID.randomUUID()).build();
            Product product = Product.builder().uid(UUID.randomUUID()).build();
            Cart cart = Cart.builder().member(member).product(product).amount(1L).build();
            Long newAmount = 5L;

            when(cartRepository.findByMemberAndProduct(member, product).get()).thenReturn(cart);
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
            Member member = Member.builder().uid(UUID.randomUUID()).build();
            Product product = Product.builder().uid(UUID.randomUUID()).build();
            Long amount = 2L;

            when(cartRepository.findByMemberAndProduct(member, product)).thenReturn(null);

            // when
            Cart result = cartService.alreadyInCart(member, product, amount);

            // then
            assertThat(result).isNull();
            verify(cartRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteCartByCartId 테스트")
    class DeleteCartByCartId {

        @Test
        @DisplayName("성공 - 카트 삭제")
        void success() {
            // given
            Long cartId = 1L;

            // when
            cartService.deleteCartByCartId(cartId);

            // then
            verify(validateService).validateCart(cartId);
            verify(cartRepository).deleteById(cartId);
        }
    }

    @Nested
    @DisplayName("getCartByMemberId 메서드")
    class GetCartByMemberId {

        @Test
        @DisplayName("성공 - 유효한 memberUid로 카트 목록 조회")
        void success() {
            // given
            UUID memberUid = UUID.randomUUID();

            List<Cart> mockCarts = List.of(
                    Cart.builder()
                            .id(1L)
                            .member(Member.builder().uid(memberUid).build())
                            .product(Product.builder().uid(UUID.randomUUID()).build())
                            .amount(2L)
                            .build(),
                    Cart.builder()
                            .id(2L)
                            .member(Member.builder().uid(memberUid).build())
                            .product(Product.builder().uid(UUID.randomUUID()).build())
                            .amount(1L)
                            .build()
            );

            when(cartRepository.findByMember_Uid(memberUid)).thenReturn(mockCarts);

            // when
            List<Cart> result = cartService.getCartByMemberUid(memberUid);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getProduct()).isNotNull();

            verify(cartRepository).findByMember_Uid(memberUid);
            verify(validateService).validateCartList(mockCarts);
        }
    }
}
