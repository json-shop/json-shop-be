package deepdive.jsonstore.domain.admin.service.order;

import deepdive.jsonstore.domain.admin.dto.OrderProductSalesResponse;
import deepdive.jsonstore.domain.admin.dto.OrderUpdateResponse;
import deepdive.jsonstore.domain.order.repository.OrderProductRepository;
import deepdive.jsonstore.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AdminOrderSerivce {

    private final OrderService orderService;
    private final OrderProductRepository orderProductRepository;

    public Page<OrderProductSalesResponse> getOrderProductSalesResponsesByPage(Long adminId, Pageable pageable) {
        return orderProductRepository.findByProductAdminId(adminId, pageable)
                .map(OrderProductSalesResponse::from);
    }

    @Transactional
    public void updateOrder(UUID orderUid, OrderUpdateResponse orderUpdateResponse, String reason) {
        var order = orderService.loadByUid(orderUid);
        order.update(orderUpdateResponse);
    }

}
