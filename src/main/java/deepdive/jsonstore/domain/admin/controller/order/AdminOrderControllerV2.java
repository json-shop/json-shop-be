package deepdive.jsonstore.domain.admin.controller.order;


import deepdive.jsonstore.domain.admin.dto.OrderProductSalesResponse;
import deepdive.jsonstore.domain.admin.dto.OrderUpdateRequest;
import deepdive.jsonstore.domain.admin.service.order.AdminOrderSerivce;
import deepdive.jsonstore.domain.order.dto.OrderResponse;
import deepdive.jsonstore.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/orders")
@RestController
public class AdminOrderControllerV2 {

    private final AdminOrderSerivce adminOrderService;
    private final OrderService orderService;

    /** 주문상품 조회 */
    @GetMapping("/{orderUid}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderUid") String orderUlid) {
        log.info("orderUlid={}", orderUlid);
        return ResponseEntity.ok(orderService.getOrderResponse(Base64.getUrlDecoder().decode(orderUlid)));
    }

    /** 주문상품 페이지 조회 */
    @GetMapping("")
    public ResponseEntity<Page<OrderProductSalesResponse>> getOrder(
            @AuthenticationPrincipal(expression = "adminUlid") byte[] adminUlid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String direction
    ) {

        log.info("adminUlid={}", adminUlid);

        var sortDirection = Sort.Direction.fromString(direction);
        var pageRequest = PageRequest.of(
                0,
                10,
                Sort.by(sortDirection, "createdAt")
        );
        return ResponseEntity.ok(adminOrderService.getOrderResponsesByPage(adminUlid, pageRequest));
    }

    @PutMapping("/{orderUid}")
    public ResponseEntity<Void> updateState(
            @PathVariable("orderUid") String orderUlid,
            @RequestBody OrderUpdateRequest orderUpdateRequest
    ) {
        log.info("orderUlid={}", orderUlid);
        adminOrderService.updateOrder(
                Base64.getUrlDecoder().decode(orderUlid),
                orderUpdateRequest,
                "관리자가 수정함"
        );
        return ResponseEntity.ok().build();
    }
}
