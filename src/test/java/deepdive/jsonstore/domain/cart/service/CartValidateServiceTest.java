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
import java.util.UUID;

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
            UUID memberUid = UUID.randomUUID();
            Member member = Member.builder().uid(memberUid).build();

            when(memberRepository.findByUid(memberUid)).thenReturn(Optional.of(member));

            Member result = cartValidateService.validateMember(memberUid);

            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("실패 - 멤버 없음")
        void fail_notFound() {
            UUID memberUid = UUID.randomUUID();
            when(memberRepository.findByUid(memberUid)).thenReturn(Optional.empty());

            assertThrows(CartException.MemberNotFoundException.class,
                    () -> cartValidateService.validateMember(memberUid));
        }
    }

    @Nested
    @DisplayName("validateProduct 테스트")
    class ValidateProduct {

        @Test
        @DisplayName("성공 - 판매중이고 재고 충분")
        void success() {
            UUID productUid = UUID.randomUUID();
            Long amount = 3L;

            Product product = Product.builder()
                    .uid(productUid)
                    .status(ProductStatus.ON_SALE)
                    .stock(10)
                    .build();

            when(productRepository.findByUid(productUid)).thenReturn(Optional.of(product));

            Product result = cartValidateService.validateProduct(productUid, amount);

            assertThat(result).isEqualTo(product);
        }

        @Test
        @DisplayName("실패 - 상품 없음")
        void fail_notFound() {
            UUID productUid = UUID.randomUUID();

            when(productRepository.findByUid(productUid)).thenReturn(Optional.empty());

            assertThrows(CartException.ProductNotFoundException.class,
                    () -> cartValidateService.validateProduct(productUid, 1L));
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

    @Nested
    @DisplayName("validateCartList 테스트")
    class ValidateCartList {

        @Test
        @DisplayName("성공 - 카트 목록이 존재하면 예외를 던지지 않는다")
        void success() {
            List<Cart> carts = List.of(
                    Cart.builder()
                            .id(1L)
                            .member(Member.builder().uid(UUID.randomUUID()).build())
                            .product(Product.builder().uid(UUID.randomUUID()).build())
                            .amount(2L)
                            .build()
            );

            assertDoesNotThrow(() -> cartValidateService.validateCartList(carts));
        }

        @Test
        @DisplayName("실패 - 카트 목록이 비어있으면 CartNotFoundException 예외 발생")
        void fail_emptyCartList() {
            List<Cart> emptyCarts = List.of();

            assertThatThrownBy(() -> cartValidateService.validateCartList(emptyCarts))
                    .isInstanceOf(CartException.CartNotFoundException.class);
        }
    }
}