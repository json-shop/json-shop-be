package deepdive.jsonstore.domain.order.controller;

import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.order.dto.*;
import deepdive.jsonstore.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
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
//            @AuthencitaitonPrincipal(expression"Member=member.id") UUID memberId,
            @RequestBody OrderRequest orderRequest) {
        var memberId = 1L; // TODO : 제거
        var orderUid = orderService.createOrder(memberId, orderRequest);
        return ResponseEntity.created(
                URI.create("/api/v1/orders/" + orderUid.toString())
        ).build();
    }

    // TODO : 권한 부여
    /** 주문 조회 */
    @GetMapping("/{orderUid}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderUid") UUID orderUid) {
        return ResponseEntity.ok(orderService.getOrder(orderUid));
    }

    /** 주문 페이지 조회 */
    @GetMapping("")
    public ResponseEntity<Page<OrderResponse>> getOrder(
//            @AuthencitaitonPrincipal(expression"Member=member.id")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var memberId = 1L; // TODO : 제거
        return ResponseEntity.ok(orderService.getOrdersByPage(memberId, PageRequest.of(0, 10)));
    }

    /** PG 결제 승인 요청 */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody ConfirmRequest confirmRequest) {
        orderService.confirmOrder(confirmRequest);
        return ResponseEntity.ok().build();
    }

    /** PG 웹훅 */
    @PostMapping("/webhook")
    public ResponseEntity<?> pgWebhook() {
        log.info("webhook");
        return null;
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
        orderService.updateOrderDeliveryBeforeShipment(orderUid, deliveryUid);
        return ResponseEntity.ok().build();
    }
}