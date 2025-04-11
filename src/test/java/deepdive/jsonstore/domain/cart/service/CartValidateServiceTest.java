package deepdive.jsonstore.domain.cart.service;

import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.exception.CartException;
import deepdive.jsonstore.domain.cart.repository.CartRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartValidateServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartValidateService cartValidateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("validateMember 테스트")
    class ValidateMember {

        @Test
        @DisplayName("성공 - 멤버 존재")
        void success() {
            Long memberId = 1L;
            Member member = Member.builder().id(memberId).build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

            Member result = cartValidateService.validateMember(memberId);

            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("실패 - 멤버 없음")
        void fail_notFound() {
            Long memberId = 2L;
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            assertThrows(CartException.MemberNotFoundException.class,
                    () -> cartValidateService.validateMember(memberId));
        }
    }

    @Nested
    @DisplayName("validateProduct 테스트")
    class ValidateProduct {

        @Test
        @DisplayName("성공 - 판매중이고 재고 충분")
        void success() {
            Long productId = 10L;
            Long amount = 3L;

            Product product = Product.builder()
                    .id(productId)
                    .status(ProductStatus.ON_SALE)
                    .stock(10)
                    .build();

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            Product result = cartValidateService.validateProduct(productId, amount);

            assertThat(result).isEqualTo(product);
        }

        @Test
        @DisplayName("실패 - 상품 없음")
        void fail_notFound() {
            Long productId = 11L;

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(CartException.ProductNotFoundException.class,
                    () -> cartValidateService.validateProduct(productId, 1L));
        }

        @Test
        @DisplayName("실패 - 상품 판매 중 아님")
        void fail_productNotOnSale() {
            Product product = Product.builder()
                    .id(12L)
                    .status(ProductStatus.DISCONTINUED)
                    .stock(10)
                    .build();

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            assertThrows(CartException.ProductForbiddenException.class,
                    () -> cartValidateService.validateProduct(product.getId(), 1L));
        }

        @Test
        @DisplayName("실패 - 재고 부족")
        void fail_outOfStock() {
            Product product = Product.builder()
                    .id(13L)
                    .status(ProductStatus.ON_SALE)
                    .stock(2)
                    .build();

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            assertThrows(CartException.ProductOutOfStockException.class,
                    () -> cartValidateService.validateProduct(product.getId(), 5L));
        }
    }

    @Nested
    @DisplayName("validateAmount 테스트")
    class ValidateAmount {

        @Test
        @DisplayName("성공 - 총 수량이 재고 이내")
        void success() {
            Cart cart = Cart.builder().amount(2L).build();
            Product product = Product.builder().stock(10).build();
            Long addAmount = 3L;

            Long result = cartValidateService.validateAmount(cart, product, addAmount);

            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("실패 - 총 수량이 재고 초과")
        void fail_exceedsStock() {
            Cart cart = Cart.builder().amount(8L).build();
            Product product = Product.builder().stock(10).build();
            Long addAmount = 5L;

            assertThrows(CartException.ProductOutOfStockException.class,
                    () -> cartValidateService.validateAmount(cart, product, addAmount));
        }
    }

    @Nested
    @DisplayName("validateCart(Long cartId) 테스트")
    class ValidateCartByCartId {

        @Test
        @DisplayName("성공 - cartId가 존재하는 경우")
        void success() {
            // given
            Long cartId = 1L;
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(mock(Cart.class)));

            // when & then
            assertDoesNotThrow(() -> cartValidateService.validateCart(cartId));
        }

        @Test
        @DisplayName("실패 - cartId가 존재하지 않는 경우")
        void fail_notFound() {
            // given
            Long cartId = 2L;
            when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(CartException.CartNotFoundException.class,
                    () -> cartValidateService.validateCart(cartId));
        }
    }

    @DisplayName("validateCart 메서드")
    class ValidateCart {

        @Test
        @DisplayName("성공 - 카트 목록이 존재하면 예외를 던지지 않는다")
        void success() {
            // given
            List<Cart> carts = List.of(
                    Cart.builder()
                            .id(1L)
                            .member(Member.builder().id(1L).build())
                            .product(Product.builder().id(10L).build())
                            .amount(2L)
                            .build()
            );

            // when & then
            assertDoesNotThrow(() -> cartValidateService.validateCartList(carts));
        }

        @Test
        @DisplayName("실패 - 카트 목록이 비어있으면 CartNotFoundException 예외 발생")
        void fail_emptyCartList() {
            // given
            List<Cart> emptyCarts = List.of();

            // when & then
            assertThatThrownBy(() -> cartValidateService.validateCartList(emptyCarts))
                    .isInstanceOf(CartException.CartNotFoundException.class);
        }
    }
}
