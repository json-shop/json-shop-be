package deepdive.jsonstore.domain.order.controller;

import deepdive.jsonstore.domain.order.dto.*;
import deepdive.jsonstore.domain.order.service.OrderService;
import deepdive.jsonstore.domain.order.service.WebhookVerificationService;
import io.portone.sdk.server.webhook.WebhookTransactionDataConfirm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final WebhookVerificationService webhookverificationSerivce;

    // 1. 주문 생성
    @PostMapping
    public ResponseEntity<Void> createOrder(
//            @AuthenticationPrincipal(expression="Member=member.id") Long memberId,
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

    // 2. 컨펌프로세스
    // 결제 최종 승인 API(포트원과 통신)
    // POST로 요청됩니다. (Timeout : 10s)
    // 타임아웃 발생시? PG측 트랜젝션 실패 처리 -> 서버도 10초 타임아웃 필요? tx_id만 바뀜
    @PostMapping("/confirm")
    public ResponseEntity<Map<String,String>> pgConfirmPayment(
            @RequestBody WebhookTransactionDataConfirm webhookTransactionDataConfirm
//            HttpServletRequest request,
//            @RequestBody String rawBody,
//            @RequestHeader("X-PORTONE-ID") String msgId,
//            @RequestHeader("X-PORTONE-SIGNATURE") String signature,
//            @RequestHeader("X-PORTONE-TIMESTAMP") String timestamp
    ) {
        ConfirmReason confirmReason =  orderService.confirmOrder(webhookTransactionDataConfirm);
        log.debug(confirmReason.getStatus().toString());
        return ResponseEntity.status(confirmReason.getStatus())
                .body(Map.of("reason", confirmReason.getReason()));
    }

    /*
    {
        "imp_uid": "imp_123456789012",
        "merchant_uid": "order-20240404-0001",
        "status": "paid",
        "amount": 50000,
          ...
     }
     */
    // 3. 웹훅으로 상태갱신
    // 결제 상태 변경 수신
    // PG사로부터 요청
    @PostMapping("/webhook")
//    public ResponseEntity<String> handleWebhook(
//            @RequestHeader Map<String, String> headers,
//            @RequestBody String body
//    ) {
//        headers.forEach((k, v) -> {
//            System.out.println(k + ": " + v);
//        });
//
//        return ResponseEntity.ok("ok");
//    }
//    public ResponseEntity<?> pgWebhook(@RequestBody WebhookRequest webhookRequest) {
    public ResponseEntity<?> pgWebhook(
            @RequestBody String rawBody,
            @RequestHeader("webhook-id") String msgId,
            @RequestHeader("webhook-signature") String signature,
            @RequestHeader("webhook-timestamp") String timestamp

    ) {
        var test = webhookverificationSerivce.verify(rawBody, msgId, signature, timestamp);
        log.info(test.toString());
        log.info(test.getData().toString());
        orderService.webhook(test.getData());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "fail", "reason", "test", "message", "일반 결제 성공"));
//        switch (webhookRequest.status()) {
//            case "paid" :
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(Map.of("status", "fail", "reason", "test", "message", "일반 결제 성공"));
//            default :
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Map.of("status", "fail", "reason", "test", "message", "실패", "errorMessage", "ㅋㅋㅋㅋㅋ"));
//        }
    }

    @PostMapping("{orderUid}/cancel")
    public ResponseEntity<?> cancel(@PathVariable("orderUid") UUID orderUid) {
        orderService.cancelOrderBeforeShipment(orderUid);
        return ResponseEntity.ok().build();
    }
}
