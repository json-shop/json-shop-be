package deepdive.jsonstore.domain.order.controller;

import deepdive.jsonstore.domain.order.dto.*;
import deepdive.jsonstore.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /** 주문 생성 */
    @PostMapping
    public ResponseEntity<Void> createOrder(
            @AuthenticationPrincipal(expression="uid") UUID memberUid,
            @RequestBody OrderRequest orderRequest) {
        var orderUid = orderService.createOrder(memberUid, orderRequest);
        return ResponseEntity.created(
                URI.create("/api/v1/orders/" + orderUid.toString())
        ).build();
    }

    /** 주문 조회 */
    @GetMapping("/{orderUid}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderUid") UUID orderUid) {
        return ResponseEntity.ok(orderService.getOrderResponse(orderUid));
    }

    /** 주문 페이지 조회 */
    @GetMapping("")
    public ResponseEntity<Page<OrderResponse>> getOrder(
            @AuthenticationPrincipal(expression="uid") UUID memberUid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String direction

    ) {
        var pageRequest = PageRequest.of(
                0,
                10,
                Sort.by(direction, "createdAt")
        );
        return ResponseEntity.ok(orderService.getOrderResponsesByPage(memberUid, pageRequest));
    }

    /** PG 결제 승인 요청 */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody ConfirmRequest confirmRequest) {
        orderService.confirmOrder(confirmRequest);
        return ResponseEntity.ok().build();
    }

    /** 주문 취소 */
    @PostMapping("/{orderUid}/cancel")
    public ResponseEntity<?> cancel(@PathVariable("orderUid") UUID orderUid) {
        orderService.cancelOrderBeforeShipment(orderUid);
        return ResponseEntity.ok().build();
    }

    /** 사용자 배송지 변경 */
    @PutMapping("/{orderUid}/delivery/{deliveryUid}")
    public ResponseEntity<?> updateOrderDelivery(
            @PathVariable("orderUid") UUID orderUid,
            @PathVariable("deliveryUid") UUID deliveryUid
    ) {
        orderService.updateOrderDeliveryBeforeShipping(orderUid, deliveryUid);
        return ResponseEntity.ok().build();
    }
}