package deepdive.jsonstore.domain.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.domain.cart.dto.CartDeleteRequest;
import deepdive.jsonstore.domain.cart.dto.CartRequest;
import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.cart.service.CartService;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class CartApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CartApiController controller = new CartApiController(cartService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested
    @DisplayName("addProductToCart API 테스트")
    class AddProductToCart {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID memberUid = UUID.randomUUID();
            UUID productUid = UUID.randomUUID();

            CartRequest request = new CartRequest(memberUid, productUid, 3L);

            Cart mockCart = Cart.builder()
                    .id(100L)
                    .member(Member.builder().uid(memberUid).build())
                    .product(Product.builder().uid(productUid).build())
                    .amount(3L)
                    .build();

            when(cartService.addProductToCart(any(UUID.class), any(UUID.class), anyLong()))
                    .thenReturn(mockCart);

            mockMvc.perform(post("/api/v1/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100L))
                    .andExpect(jsonPath("$.amount").value(3));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 요청 (memberUid 없음)")
        void fail_invalidRequest() throws Exception {
            String invalidJson = """
                {
                    "productUid": "123e4567-e89b-12d3-a456-426614174000",
                    "amount": 2
                }
            """;

            mockMvc.perform(post("/api/v1/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("deleteCartByMemberId API 테스트")
    class DeleteCartByMemberId {

        @Test
        @DisplayName("성공 - 장바구니 항목 삭제")
        void success() throws Exception {
            CartDeleteRequest request = new CartDeleteRequest(5L);

            mockMvc.perform(delete("/api/v1/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(cartService).deleteCartByCartId(5L);
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 요청 (cartId 누락)")
        void fail_invalidRequest() throws Exception {
            String invalidJson = "{}";

            mockMvc.perform(delete("/api/v1/carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(cartService, never()).deleteCartByCartId(anyLong());
        }
    }

    @Nested
    @DisplayName("getCartByMemberId API 테스트")
    class GetCartByMemberId {

        @Test
        @DisplayName("성공 - 장바구니 목록 조회")
        void success() throws Exception {
            UUID memberUid = UUID.randomUUID();
            UUID productUid = UUID.randomUUID();

            Cart cart = Cart.builder()
                    .id(1L)
                    .member(Member.builder().uid(memberUid).build())
                    .product(Product.builder().uid(productUid).build())
                    .amount(2L)
                    .build();

            when(cartService.getCartByMemberId(any(UUID.class)))
                    .thenReturn(List.of(cart));

            mockMvc.perform(get("/api/v1/carts")
                            .param("memberUid", memberUid.toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].amount").value(2));

            verify(cartService).getCartByMemberId(memberUid);
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 요청 (memberUid 누락)")
        void fail_invalidRequest() throws Exception {
            mockMvc.perform(get("/api/v1/carts")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
