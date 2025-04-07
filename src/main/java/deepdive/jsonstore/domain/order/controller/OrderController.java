package deepdive.jsonstore.domain.order.controller;

import deepdive.jsonstore.domain.order.dto.OrderRequest;
import deepdive.jsonstore.domain.order.dto.OrderResponse;
import deepdive.jsonstore.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<Map<?,?>> createOrder(
//            @AuthencitaitonPrincipal(expression"Member=member.id")
            UUID memberId,
            OrderRequest orderRequest) {
        var orderUid = orderService.createOrder(memberId, orderRequest);
        return ResponseEntity.created(
                URI.create("/api/v1/orders/" + orderUid.toString())
        ).body(Map.of("result", true));
    }

    // 주문 조회
    // TODO : 권한 부여
    @GetMapping("/{orderUid}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderUid") UUID orderUid) {
        return ResponseEntity.ok(orderService.getOrder(orderUid));
    }

    // 웹훅
    /*
    {
        "imp_uid": "imp_123456789012",
        "merchant_uid": "order-20240404-0001",
        "status": "paid",
        "amount": 50000,
          ...
     }
     */

    // 주문서 검증(리퀘스트파람으로 들어옴)

}
