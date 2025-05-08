package deepdive.jsonstore.domain.order.controller;

import de.huxhorn.sulky.ulid.ULID;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.order.dto.*;
import deepdive.jsonstore.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/orders")
public class OrderControllerV2 {

    private final OrderService orderService;

    /** 주문 생성 */
    @PostMapping
    public ResponseEntity<Void> createOrder(
            @AuthenticationPrincipal(expression="ulid") byte[] memberUlid,
            @RequestBody OrderRequestV2 orderRequestV2) {
        log.info("memberUlid={}", Base64.getUrlEncoder().encodeToString(memberUlid));
        var orderUid = orderService.createOrder(memberUlid, orderRequestV2);
        return ResponseEntity.created(
                URI.create("/api/v1/orders/" + Base64.getUrlEncoder().encodeToString(orderUid))
        ).build();
    }

    /** 주문 조회 */
    @GetMapping("/{orderUid}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderUid") String orderUlid) {
        log.info("orderUlid={}",orderUlid);
        return ResponseEntity.ok(orderService.getOrderResponse(Base64.getUrlDecoder().decode(orderUlid)));
    }

    /** 주문 페이지 조회 */
    @GetMapping("")
    public ResponseEntity<Page<OrderResponse>> getOrder(
            @AuthenticationPrincipal(expression="ulid") byte[] memberUlid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        var sortDirection = Sort.Direction.fromString(direction);
        var pageRequest = PageRequest.of(
                0,
                10,
                Sort.by(sortDirection, "createdAt")
        );

        log.info("memberUlid={}", Base64.getUrlEncoder().encodeToString(memberUlid));
        return ResponseEntity.ok(orderService.getOrderResponsesByPage(memberUlid, pageRequest));
    }

    /** PG 결제 승인 요청 */
    @PermitAll
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody ConfirmRequest confirmRequest) {
        log.info("confirm={}", confirmRequest);
        orderService.confirmOrderV2(confirmRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


    /** 주문 취소 */
    @PostMapping("/{orderUid}/cancel")
    public ResponseEntity<?> cancel(@PathVariable("orderUid") String orderUlid) {
        log.info("orderUlid={}", orderUlid);
        orderService.cancelOrderBeforeShipment(Base64.getUrlDecoder().decode(orderUlid));
        return ResponseEntity.ok().build();
    }

    /** 사용자 배송지 변경 */
    @PutMapping("/{orderUid}/delivery/{deliveryUid}")
    public ResponseEntity<?> updateOrderDelivery(
            @PathVariable("orderUid") String orderUlid,
            @PathVariable("deliveryUid") String deliveryUlid
    ) {
        orderService.updateOrderDeliveryBeforeShipping(
                Base64.getUrlDecoder().decode(orderUlid),
                Base64.getUrlDecoder().decode(deliveryUlid)
        );
        return ResponseEntity.ok().build();
    }

    @PermitAll
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody WebhookRequest webhookRequest) {
        log.info("webhookRequest={}", webhookRequest);
        orderService.webhook(webhookRequest);
        return ResponseEntity.ok().build();
    }
}