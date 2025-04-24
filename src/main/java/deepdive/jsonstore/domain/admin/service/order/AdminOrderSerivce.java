package deepdive.jsonstore.domain.admin.service.order;

import deepdive.jsonstore.domain.admin.dto.OrderProductSalesResponse;
import deepdive.jsonstore.domain.admin.dto.OrderUpdateRequest;
import deepdive.jsonstore.domain.admin.service.AdminValidationService;
import deepdive.jsonstore.domain.order.repository.OrderProductRepository;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
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
    private final AdminValidationService adminValidationService;

    public Page<OrderProductSalesResponse> getOrderResponsesByPage(UUID adminUid, Pageable pageable) {
        var admin = adminValidationService.getAdminById(adminUid);
        return orderProductRepository.findByAdminId(admin.getId(), pageable)
                .map(OrderProductSalesResponse::from);
    }

    public Page<OrderProductSalesResponse> getOrderResponsesByPage(byte[] adminUlid, Pageable pageable) {
        var admin = adminValidationService.getAdminById(adminUlid);
        return orderProductRepository.findByAdminId(admin.getId(), pageable)
                .map(OrderProductSalesResponse::from);
    }

    @Transactional
    public void updateOrder(UUID orderUid, OrderUpdateRequest orderUpdateRequest, String reason) {
        var order = orderService.loadByUid(orderUid);
        order.update(orderUpdateRequest);
    }

    @Transactional
    public void updateOrder(byte[] orderUlid, OrderUpdateRequest orderUpdateRequest, String reason) {
        var order = orderService.loadByUid(orderUlid);
        order.update(orderUpdateRequest);
    }

}
