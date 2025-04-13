package deepdive.jsonstore.domain.admin.service.order;

import deepdive.jsonstore.domain.admin.dto.OrderProductSalesResponse;
import deepdive.jsonstore.domain.admin.dto.OrderUpdateResponse;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import deepdive.jsonstore.domain.order.repository.OrderProductRepository;
import deepdive.jsonstore.domain.order.service.OrderService;
import deepdive.jsonstore.domain.product.entity.Product;
import org.hibernate.query.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderSerivceTest {

    @InjectMocks
    AdminOrderSerivce adminOrderSerivce;

    @Mock
    OrderProductRepository orderProductRepository;

    @Mock
    OrderService orderService;

    @Nested
    @DisplayName("관리자주문 조회(판매내역)")
    class getOrderProductSalesResponsesByPage {
        @Test
        @DisplayName("성공")
        void getOrderProductSalesResponsesByPage_성공() {
            // Given
            var adminId = 1L;
            var orderUid = UUID.randomUUID();
            var pageable = PageRequest.of(0, 10);
            var mockProduct = Product.builder()
                    .admin(Admin.builder().id(adminId).build())
                    .build();
            var mockOrderProduct = OrderProduct.builder()
                    .product(mockProduct)
                    .order(Order.builder().uid(orderUid).build())
                    .build();

            var mockPage = new PageImpl<>(List.of(mockOrderProduct));

            when(orderProductRepository.findByProductAdminId(adminId, pageable)).thenReturn(mockPage);

            // When
            var result = adminOrderSerivce.getOrderProductSalesResponsesByPage(adminId, pageable);

            // Then
            assertThat(result)
                    .isNotNull();
            assertThat(result.getSize())
                    .isEqualTo(1);
            verify(orderProductRepository, times(1)).findByProductAdminId(adminId, pageable);
        }
        @Test
        @DisplayName("조회 결과 없음")
        void getOrderProductSalesResponsesByPage_결과없음() {
            // Given
            var adminId = 1L;
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<OrderProduct>(List.of(), pageable, 0);

            when(orderProductRepository.findByProductAdminId(adminId, pageable)).thenReturn(emptyPage);

            // When
            var result = adminOrderSerivce.getOrderProductSalesResponsesByPage(adminId, pageable);

            // Then
            assertThat(result).isEmpty();
            verify(orderProductRepository).findByProductAdminId(adminId, pageable);
        }
    }

    @Nested
    @DisplayName("관리자주문 수정")
    class updateOrder {

        @Test
        @DisplayName("성공")
        void updateOrder_shouldUpdateOrderWithReason() {
            // Given
            var orderUid = UUID.randomUUID();
            var updateResponse = OrderUpdateResponse.builder()
                    .status(OrderStatus.EXPIRED)
                    .build();
            var reason = "Address updated by admin";

            var mockOrder = mock(Order.class);

            when(orderService.loadByUid(orderUid)).thenReturn(mockOrder);

            // When
            adminOrderSerivce.updateOrder(orderUid, updateResponse, reason);

            // Then
            verify(orderService, times(1)).loadByUid(orderUid);
            verify(mockOrder, times(1)).update(updateResponse);
        }
    }
}