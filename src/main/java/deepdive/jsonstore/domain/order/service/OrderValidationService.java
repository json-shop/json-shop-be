package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.CustomException;
import deepdive.jsonstore.domain.order.model.Order;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderValidationService {

    private final OrderRepository orderRepository;

    //DTO가 아닌 엔티티를 반환한다?
    public Order getByUuid(UUID uuid) {
        return orderRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "찾을 수 없음"));
    }
}
