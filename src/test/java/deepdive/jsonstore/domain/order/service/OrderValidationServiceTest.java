package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import deepdive.jsonstore.domain.order.exception.OrderException;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 이거 꼭 추가!
class OrderValidationServiceTest {

    @InjectMocks
    private OrderValidationService orderValidationService;

    @Mock
    private ProductValidationService productValidationService;

    @Nested
    @DisplayName("주문 만료 검증")
    class validateExpiration {
        @ParameterizedTest
        @ValueSource(strings = {"CREATED", "PAYMENT_PENDING", "PAID", "IN_DELIVERY", "PREPARING_SHIPMENT", "DONE"})
        void validateExpriration_성공(String statusName) {
            //given
            var status = OrderStatus.valueOf(statusName);
            var orderUid = UUID.randomUUID();
            var mockOrder = Order.builder()
                    .uid(orderUid)
                    .orderStatus(status)
                    .build();
            //when & then
            assertDoesNotThrow(() -> orderValidationService.validateExpiration(mockOrder));
        }
        @ParameterizedTest
        @ValueSource(strings = {"EXPIRED", "CANCELED", "FAILED"})
        void validateExpriration_실패(String statusName) {
            //given
            var status = OrderStatus.valueOf(statusName);
            var orderUid = UUID.randomUUID();
            var mockOrder = Order.builder()
                    .uid(orderUid)
                    .orderStatus(status)
                    .build();
            //when & then
            assertThrows(OrderException.OrderExpiredException.class,
                    ()-> orderValidationService.validateExpiration(mockOrder));
        }
    }

    @Nested
    @DisplayName("주문 재고 검증")
    class validateProductStock {

        @Test
        void validateProductStock_성공() {
            //given
            var puid = UUID.randomUUID();
            var product = Product.builder()
                    .uid(puid)
                    .stock(1)
                    .build();
            var orderProduct = OrderProduct.builder()
                    .product(product)
                    .quantity(1)
                    .build();
            var mockOrder = Order.builder()
                    .orderProducts(List.of(orderProduct))
                    .build();

            //when
            when(productValidationService.findActiveProductById(puid)).thenReturn(product);

            //then
            assertDoesNotThrow(() -> orderValidationService.validateProductStock(mockOrder));
        }

        @Test
        void validateProductStock_실패() {
            //given
            var name = "테스트";
            var puid = UUID.randomUUID();
            var product = Product.builder()
                    .uid(puid)
                    .name(name)
                    .stock(1)
                    .build();
            var orderProduct = OrderProduct.builder()
                    .product(product)
                    .quantity(2)
                    .build();
            var mockOrder = Order.builder()
                    .orderProducts(List.of(orderProduct))
                    .build();

            //when
            when(productValidationService.findActiveProductById(puid)).thenReturn(product);

            //then
            assertThrows(OrderException.OrderOutOfStockException.class,
                    () -> orderValidationService.validateProductStock(mockOrder))
                    .getExtra().getFirst().equals(name);
        }
    }

    @Nested
    @DisplayName("주문 배송전 검증")
    class validateBeforeShipping {

        @ParameterizedTest
        @ValueSource(strings = {"CREATED", "PAYMENT_PENDING", "PAID", "PREPARING_SHIPMENT"})
        void validateBeforeShipping_성공(String statusName) {
            //given
            var status = OrderStatus.valueOf(statusName);
            var order = Order.builder()
                    .orderStatus(status)
                    .build();

            //when & then
            assertDoesNotThrow(() -> orderValidationService.validateBeforeShipping(order));
        }

        @ParameterizedTest
        @ValueSource(strings = {"IN_DELIVERY", "DONE"})
        void validateBeforeShipping_실패(String statusName) {
            //given
            var status = OrderStatus.valueOf(statusName);
            var order = Order.builder()
                    .orderStatus(status)
                    .build();

            //when & then
            assertThrows(OrderException.AlreadyStartDeliveryException.class,
                    () -> orderValidationService.validateBeforeShipping(order));
        }

        @Nested
        @DisplayName("주문 결제전 검증")
        class validateBeforePayment {

            @ParameterizedTest
            @ValueSource(strings = {"IN_DELIVERY", "DONE", "PAID", "PREPARING_SHIPMENT"})
            void validateBeforeShipping_성공(String statusName) {
                //given
                var status = OrderStatus.valueOf(statusName);
                var order = Order.builder()
                        .orderStatus(status)
                        .build();

                //when & then
                assertDoesNotThrow(() -> orderValidationService.validateBeforePayment(order));
            }

            @ParameterizedTest
            @ValueSource(strings = {"CREATED", "PAYMENT_PENDING"})
            void validateBeforeShipping_실패(String statusName) {
                //given
                var status = OrderStatus.valueOf(statusName);
                var order = Order.builder()
                        .orderStatus(status)
                        .build();

                //when & then
                assertThrows(OrderException.NotPaidException.class,
                        () -> orderValidationService.validateBeforePayment(order));
            }
        }
    }
}
