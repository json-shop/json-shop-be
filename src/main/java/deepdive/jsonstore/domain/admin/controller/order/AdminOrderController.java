package deepdive.jsonstore.domain.admin.controller.order;


import deepdive.jsonstore.domain.admin.dto.OrderProductSalesResponse;
import deepdive.jsonstore.domain.admin.dto.OrderUpdateResponse;
import deepdive.jsonstore.domain.admin.service.order.AdminOrderSerivce;
import deepdive.jsonstore.domain.order.dto.OrderProductResponse;
import deepdive.jsonstore.domain.order.dto.OrderRequest;
import deepdive.jsonstore.domain.order.dto.OrderResponse;
import deepdive.jsonstore.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/orders")
@RestController
public class AdminOrderController {

    private final AdminOrderSerivce adminOrderService;
    private final OrderService orderService;

    /** 주문상품 조회 */
    @GetMapping("/{orderUid}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderUid") UUID orderUid) {
        return ResponseEntity.ok(orderService.getOrderResponse(orderUid));
    }

    /** 주문상품 페이지 조회 */
    @GetMapping("")
    public ResponseEntity<Page<OrderProductSalesResponse>> getOrder(
            @AuthenticationPrincipal(expression = "adminUid") Long adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String direction

    ) {
        var pageRequest = PageRequest.of(
                0,
                10,
                Sort.by(direction, "createdAt")
        );
        return ResponseEntity.ok(adminOrderService.getOrderResponsesByPage(adminId, pageRequest));
    }

    @PutMapping("/{orderUid}")
    public ResponseEntity<Void> updateState(
            @PathVariable("orderUid") UUID orderUid,
            @RequestBody OrderUpdateResponse orderUpdateResponse
    ) {
        // TODO : Log 테이블?
        adminOrderService.updateOrder(orderUid, orderUpdateResponse, "관리자가 수정함");
        return ResponseEntity.ok().build();
    }
}
