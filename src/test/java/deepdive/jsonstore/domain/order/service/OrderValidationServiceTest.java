package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.domain.order.exception.OrderException;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 이거 꼭 추가!
class OrderValidationServiceTest {

    @InjectMocks
    private OrderValidationService orderValidationService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void getByUid_성공() {
        // given
        UUID uuid = UUID.randomUUID();
        Order mockOrder = mock(Order.class);
        when(orderRepository.findByUid(uuid)).thenReturn(Optional.of(mockOrder));

        // when
        Order result = orderValidationService.findByUid(uuid);

        // then
        assertNotNull(result);
        assertEquals(mockOrder, result);
        verify(orderRepository, times(1)).findByUid(uuid);
    }

    @Test
    void getByUid_실패() {
        // given
        UUID uuid = UUID.randomUUID();
        when(orderRepository.findByUid(uuid)).thenReturn(Optional.empty());

        // when & then
        assertThrows(OrderException.OrderNotFound.class, () -> orderValidationService.findByUid(uuid));
        verify(orderRepository, times(1)).findByUid(uuid);
    }
}
