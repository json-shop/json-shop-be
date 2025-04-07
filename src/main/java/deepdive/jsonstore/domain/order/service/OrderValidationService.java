package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.OrderException;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderValidationService {

    private final OrderRepository orderRepository;

    public Order getByUuid(UUID uuid) {
        return orderRepository.findByUuid(uuid).orElseThrow(OrderException.OrderNotFound::new);
    }
}
