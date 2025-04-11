package deepdive.jsonstore.domain.order.controller;

import deepdive.jsonstore.domain.order.dto.*;
import deepdive.jsonstore.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // 1. 주문 생성
    @PostMapping
    public ResponseEntity<Void> createOrder(
//            @AuthencitaitonPrincipal(expression"Member=member.id")
//            UUID memberId,
            @RequestBody OrderRequest orderRequest) {
        var memberId = 1L;
        var orderUid = orderService.createOrder(memberId, orderRequest);
        return ResponseEntity.created(
                URI.create("/api/v1/orders/" + orderUid.toString())
        ).build();
    }

    // 4. 주문 조회
    // TODO : 권한 부여
    @GetMapping("/{orderUid}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderUid") UUID orderUid) {
        return ResponseEntity.ok(orderService.getOrder(orderUid));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody ConfirmRequest confirmRequest) {
        orderService.confirmOrder(confirmRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> pgWebhook() {
        log.info("webhook");
        return null;
    }

    @PostMapping("/{orderUid}/cancel")
    public ResponseEntity<?> cancel(@PathVariable("orderUid") UUID orderUid) {
        orderService.cancelOrderBeforeShipment(orderUid);
        return ResponseEntity.ok().build();
    }
}