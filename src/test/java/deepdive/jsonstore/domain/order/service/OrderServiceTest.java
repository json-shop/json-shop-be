package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.service.DeliveryService;
import deepdive.jsonstore.domain.notification.service.NotificationService;
import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.order.exception.OrderException;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.service.MemberValidationService;
import deepdive.jsonstore.domain.order.dto.*;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.exception.ProductException;
import deepdive.jsonstore.domain.product.service.ProductStockService;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ProductValidationService productValidationService;

    @Mock
    MemberValidationService memberValidationService;

    @Mock
    OrderValidationService orderValidationService;

    @Mock
//    @Spy
    ProductStockService productStockService;

    @Mock
    PaymentService paymentService;

    @Mock
    NotificationService notificationService;

    @Mock
    private DeliveryService deliveryService;

    @Nested
    class loadByUid {
        @Test
        @DisplayName("uid로 주문 불러오기 검증")
        void loadByUid_성공() {
            // given
            UUID orderUid = UUID.randomUUID();
            Order order = Order.builder().uid(orderUid).build();

            // when
            when(orderRepository.findByUid(orderUid)).thenReturn(Optional.of(order));

            // then
            Order result = orderService.loadByUid(orderUid);
            assertThat(result).isEqualTo(result);
            verify(orderRepository, times(1)).findByUid(orderUid);
        }

        @Test
        void loadByUid_존재하지않는_UID_실패() {
            // given
            UUID uid = UUID.randomUUID();
            when(orderRepository.findByUid(uid)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.loadByUid(uid))
                    .isInstanceOf(OrderException.OrderNotFound.class);
            verify(orderRepository, times(1)).findByUid(uid);
        }
    }

    @Nested
    @DisplayName("주문 생성")
    class createOrder {

        @Test
        @DisplayName("성공")
        void createOrder_성공() {
            // given
            UUID memberUid = UUID.randomUUID();
            UUID productUid = UUID.randomUUID();

            Member member = Member.builder()
                    .uid(memberUid)
                    .build();

            Product product = Product.builder()
                    .uid(productUid)
                    .price(10000)
                    .stock(10)
                    .build();

            OrderRequest orderRequest = OrderRequest.builder()
                    .orderProductRequests(List.of(
                            OrderProductRequest.builder()
                                    .productUid(productUid)
                                    .quantity(2)
                                    .build()
                    ))
                    .build();

            Order savedOrder = Order.builder()
                    .uid(UUID.randomUUID())
                    .member(member)
                    .orderStatus(OrderStatus.PAYMENT_PENDING)
                    .total(20000)
                    .expiredAt(LocalDateTime.now().plusMinutes(1))
                    .build();

            when(memberValidationService.findByUid(member.getUid())).thenReturn(member); // mock 설정
            when(productValidationService.findActiveProductById(productUid)).thenReturn(product);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            // when
            UUID result = orderService.createOrder(member.getUid(), orderRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(savedOrder.getUid()).isEqualTo(result);

            verify(orderRepository, times(1)).save(any(Order.class));
            verify(memberValidationService).findByUid(member.getUid());
            verify(productValidationService).findActiveProductById(productUid);
        }

        @Test
        @DisplayName("실패-재고부족")
        void create_재고부족() {
            // given
            UUID memberUid = UUID.randomUUID();
            UUID productUid = UUID.randomUUID();

            Member member = Member.builder()
                    .uid(memberUid)
                    .build();

            Product product = Product.builder()
                    .uid(productUid)
                    .price(10000)
                    .stock(1)
                    .build();

            OrderRequest orderRequest = OrderRequest.builder()
                    .orderProductRequests(List.of(
                            OrderProductRequest.builder()
                                    .productUid(productUid)
                                    .quantity(2)
                                    .build()
                    ))
                    .build();

            when(memberValidationService.findByUid(member.getUid())).thenReturn(member); // mock 설정
            when(productValidationService.findActiveProductById(productUid)).thenReturn(product);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(member.getUid(), orderRequest))
                    .isInstanceOf(OrderException.OrderOutOfStockException.class);

            verify(memberValidationService).findByUid(member.getUid());
            verify(productValidationService).findActiveProductById(productUid);
        }

        //
        @Nested
        @DisplayName("주문 조회")
        class getOrderResponse {

            @Test
            @DisplayName("성공")
            void getOrderResponse_성공() {
                //given
                var orderUid = UUID.randomUUID();
                var member = Member.builder().build();

                var order = Order.builder()
                        .uid(orderUid)
                        .member(member)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .expiredAt(LocalDateTime.now().plusMinutes(15))
                        .build();

                //when
                when(orderRepository.findByUid(orderUid)).thenReturn(Optional.of(order));

                //then
                var orderResponse = orderService.getOrderResponse(orderUid);

                assertThat(orderResponse.orderUid()).isEqualTo(orderUid);
            }

            @Test
            @DisplayName("실패-만료")
            void getOrderResponse_만료() {
                //given
                var orderUid = UUID.randomUUID();
                var member = Member.builder().build();

                var order = Order.builder()
                        .uid(orderUid)
                        .member(member)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .expiredAt(LocalDateTime.now())
                        .build();
                //when
                when(orderRepository.findByUid(orderUid)).thenReturn(Optional.of(order));
                doThrow(new OrderException.OrderExpiredException())
                        .when(orderValidationService)
                        .validateExpiration(order);
                //then
                assertThatThrownBy(() -> orderService.getOrderResponse(orderUid))
                        .isInstanceOf(OrderException.OrderExpiredException.class);

            }
        }

        @Nested
        @DisplayName("컨펌 프로세스")
        class confirmOrder {
            @Test
            @DisplayName("성공")
            void confirmOrder_성공() {
                //given
                var product = Product.builder()
                        .stock(10)
                        .build();

                List<OrderProduct> products = List.of(OrderProduct.builder()
                        .product(product)
                        .price(100)
                        .quantity(1)
                        .build());

                var order = Order.builder()
                        .id(1L)
                        .member(Member.builder().id(1L).build())
                        .expiredAt(LocalDateTime.now().plusMinutes(1))
                        .orderProducts(products)
                        .total(100)
                        .build();
                var paymentKey = "test";
                var confirmRequest = ConfirmRequest.builder()
                        .orderId(order.getUid().toString())
                        .paymentKey(paymentKey)
                        .amount(100L)
                        .build();

                //when

                when(orderRepository.findWithLockByUid(order.getUid())).thenReturn(Optional.of(order));
                when(paymentService.confirm(confirmRequest)).thenReturn(Map.of("paymentKey", paymentKey));

                //then
                orderService.confirmOrder(confirmRequest);
                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
//                assertThat(product.getStock()).isEqualTo(9);
            }

            @Test
            @DisplayName("결제금 불일치")
            void confirmOrder_결제금_불일치() {
                //given
                var product = Product.builder()
                        .stock(10)
                        .build();

                List<OrderProduct> products = List.of(OrderProduct.builder()
                        .product(product)
                        .quantity(1)
                        .build());

                var order = Order.builder()
                        .id(1L)
                        .member(Member.builder().id(1L).build())
                        .expiredAt(LocalDateTime.now().plusMinutes(1))
                        .orderProducts(products)
                        .total(100)
                        .build();
                var paymentKey = "test";
                var confirmRequest = ConfirmRequest.builder()
                        .orderId(order.getUid().toString())
                        .paymentKey(paymentKey)
                        .amount(200L)
                        .build();

                //when
                when(orderRepository.findWithLockByUid(order.getUid())).thenReturn(Optional.of(order));
                //then
                assertThatThrownBy(()->orderService.confirmOrder(confirmRequest))
                        .isInstanceOf(OrderException.OrderTotalMismatchException.class);
            }
            @Test
            @DisplayName("재고검사")
            void confirmOrder_재고검사실패() {
                //given
                var product = Product.builder()
                        .stock(10)
                        .build();

                List<OrderProduct> products = List.of(OrderProduct.builder()
                        .product(product)
                        .quantity(1)
                        .build());

                var order = Order.builder()
                        .id(1L)
                        .member(Member.builder().id(1L).build())
                        .expiredAt(LocalDateTime.now().plusMinutes(1))
                        .orderProducts(products)
                        .total(100)
                        .build();
                var paymentKey = "test";
                var confirmRequest = ConfirmRequest.builder()
                        .orderId(order.getUid().toString())
                        .paymentKey(paymentKey)
                        .amount(100L)
                        .build();

                //when
                when(orderRepository.findWithLockByUid(order.getUid())).thenReturn(Optional.of(order));
                doThrow(new OrderException.OrderOutOfStockException())
                        .when(orderValidationService)
                        .validateProductStock(order);
                //then
                assertThatThrownBy(()->orderService.confirmOrder(confirmRequest))
                        .isInstanceOf(OrderException.OrderOutOfStockException.class);
            }
            @Test
            @DisplayName("재고예약실패_주문순간에_상품비활성화")
            void confirmOrder_재고예약실패_주문순간에_상품비활성화() {
                //given
                var product = Product.builder()
                        .id(1L)
                        .stock(10)
                        .price(100)
                        .status(ProductStatus.ON_SALE)
                        .build();

                List<OrderProduct> products = List.of(OrderProduct.builder()
                        .id(1L)
                        .product(product)
                        .quantity(1)
                        .build());

                var order = Order.builder()
                        .id(1L)
                        .member(Member.builder().id(1L).build())
                        .expiredAt(LocalDateTime.now().plusMinutes(1))
                        .orderProducts(products)
                        .total(100)
                        .build();
                var paymentKey = "test";
                var confirmRequest = ConfirmRequest.builder()
                        .orderId(order.getUid().toString())
                        .paymentKey(paymentKey)
                        .amount(100L)
                        .build();
                var op = order.getOrderProducts().getFirst();
                //when
                when(orderRepository.findWithLockByUid(order.getUid())).thenReturn(Optional.of(order));
                doThrow(new ProductException.ProductForbiddenException())
                        .when(productStockService)
                        .consumeStock(order);
//                when(paymentService.confirm(confirmRequest)).thenReturn(Map.of("paymentKey", paymentKey));

                //then
                assertThatThrownBy(() -> orderService.confirmOrder(confirmRequest))
                        .isInstanceOf(ProductException.ProductForbiddenException.class);
//                assertThat(product.getStock()).isEqualTo(9);
            }
        }

        @Nested
        @DisplayName("주문전 재고 취소")
        class cancelOrderBeforeShipment {
            @Test
            @DisplayName("성공")
            void cancelOrderBeforeShipment_성공() {
                // given
                var orderUid = UUID.randomUUID();
                var productUid = UUID.randomUUID();
                var product = Product.builder()
                        .uid(productUid)
                        .stock(0)
                        .price(100)
                        .build();
                List<OrderProduct> op = List.of(OrderProduct.builder()
                        .product(product)
                        .price(100)
                        .quantity(1)
                        .build()
                );
                var order = Order.builder()
                        .uid(orderUid)
                        .orderProducts(op)
                        .build();
                // when
                when(orderRepository.findByUid(orderUid)).thenReturn(Optional.of(order));
                // then
                orderService.cancelOrderBeforeShipment(orderUid);
                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
//                assertThat(product.getStock()).isEqualTo(1);
            }
        }

        @Nested
        @DisplayName("주문전 재고 취소")
        class updateOrderDeliveryBeforeShipping {
            @Test
            @DisplayName("성공")
            void updateOrderDeliveryBeforeShipping_성공() {
                // given
                var orderUid = UUID.randomUUID();
                var deliveryUid = UUID.randomUUID();
                var delivery = Delivery.builder()
                        .uid(deliveryUid)
                        .address("test2")
                        .build();
                var order = Order.builder()
                        .uid(orderUid)
                        .address("test1")
                        .build();
                // when
                when(orderRepository.findByUid(orderUid)).thenReturn(Optional.of(order));
                when(deliveryService.getDeliveryByUid(deliveryUid)).thenReturn(delivery);
                // then
                orderService.updateOrderDeliveryBeforeShipping(orderUid,deliveryUid);
                assertThat(order.getAddress()).isEqualTo(delivery.getAddress());
                assertThat(order.getZipCode()).isEqualTo(delivery.getZipCode());
                assertThat(order.getPhone()).isEqualTo(delivery.getPhone());
                assertThat(order.getRecipient()).isEqualTo(delivery.getRecipient());
            }
        }
    }
}
