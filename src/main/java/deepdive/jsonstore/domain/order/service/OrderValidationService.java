package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.domain.order.exception.OrderException;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderValidationService {

    private final OrderRepository orderRepository;

    public Order findByUid(UUID uid) {
        return orderRepository.findByUid(uid).orElseThrow(OrderException.OrderNotFound::new);
    }
}
