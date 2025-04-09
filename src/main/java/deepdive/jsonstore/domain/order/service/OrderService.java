package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.OrderException;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.service.MemberValidationService;
import deepdive.jsonstore.domain.notification.service.NotificationService;
import deepdive.jsonstore.domain.order.dto.*;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.service.ProductStockService;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransaction;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductValidationService productValidationService;
    private final ProductStockService productStockService;
    private final OrderValidationService orderValidationService;
    private final MemberValidationService memberValidationService;
    private final NotificationService notificationService;
    @Value("${order.expire-minutes}")
    private int ORDER_EXPIRE_TIME;
    @Value("${portone.webhook.secret-key}")
    private String key;

    /**
     * 주문서 조회
     *
     * @param orderUid 주문 uid
     * @return 주문서 Dto
     */
    public OrderResponse getOrder(UUID orderUid) {

        // 주문서 조회
        var foundOrder = orderValidationService.findByUid(orderUid);

        // 주문서 만료 처리
        if (foundOrder.getExpiredAt().isBefore(LocalDateTime.now())) {
            foundOrder.expire();
        }

        // 만료된 주문 접근시에 에러
        if (foundOrder.getOrderStatus().equals(OrderStatus.EXPIRED)) {
            throw new OrderException.OrderExpiredException();
        }

        return OrderResponse.from(foundOrder);
    }


    /**
     * 재고를 확인하고 주문서를 생성합니다.
     *
     * @param memberId     주문자 아이디
     * @param orderRequest 주문 요청 Dto
     * @return 주문서 Dto
     */
    @Transactional
    public UUID createOrder(UUID memberId, OrderRequest orderRequest) {

        //TODO : 커밋 시에 변경
//        var member = memberValidationService.findByUid(memberId);
        Member member = null;

        List<OrderProduct> orderProducts = new ArrayList<>();
        int total = 0;

        for (OrderProductRequest orderProductReq : orderRequest.orderProductRequests()) {
            var product = productValidationService.findActiveProductById(orderProductReq.productUid());

            int quantity = orderProductReq.quantity();
            int price = product.getPrice();

            // 재고 1차 검증(2차는 승인 때)
            // productService.checkStock(product);
            // TODO : 먼저 검증 반복문 추가할 것
            // TODO : 재고가 부족한 목록을 에러로 반환할 것. 익셉션에 T extra 추가
            if (product.getStock() < quantity) {
                throw new OrderException.OrderOutOfStockException();
            }

            // 리스트에 추가
            orderProducts.add(OrderProduct.from(product, quantity));

            // 총액 계산
            total += price * quantity;
        }

        // 주문 생성 및 저장
        Order order = Order.builder()
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .member(member)
                .phone(orderRequest.phone())
                .recipient(orderRequest.recipient())
                .address(orderRequest.address())
                .zipCode(orderRequest.zipCode())
                .products(orderProducts)
                .total(total)
                .expiredAt(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_TIME))
                .build();

        var savedOrder = orderRepository.save(order);

        return savedOrder.getUid();
    }

    // 요청을 검증합니다.
    public void verifyPgWebhook(String rawBody, String msgId, String signature, String timestamp) {
        var verifier = new WebhookVerifier(key);
        Webhook webhook;
        try {
            webhook = verifier.verify(rawBody, msgId, signature, timestamp);
        } catch (WebhookVerificationException e) {
            throw new RuntimeException("변조" + e.getLocalizedMessage());
        }
        if (!(webhook instanceof WebhookTransaction)) {
            throw new RuntimeException("웹훅트랜젝션이 아님");
        }
    }

    @Transactional
    public ConfirmReason confirmOrder(ConfirmRequest confirmRequest) {
//        /*
//        paymentId: 고객사에서 채번한 결제건의 고유 주문 번호입니다.
//        transactionId: 포트원에서 채번한 결제건의 고유 거래 번호입니다.
//        totalAmount: 결제건의 결제 요청 금액입니다.
//         */
//        var orderUid = confirmRequest.data().paymentId();
//        Order order;
//        try {
//           order = orderValidationService.findByUid(orderUid);
//        } catch (OrderException.OrderNotFound e) {
//            return ConfirmReason.NOT_FOUND;
//        }
//        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
//            return ConfirmReason.EXPIRED_ORDER;
//        }
//        if (confirmRequest.data().totalAmount() != order.getTotal()) {
//            return ConfirmReason.TOTAL_MISMATCH;
//        }
        var orderUid = UUID.fromString(confirmRequest.merchant_uid());
        Order order;
        try {
           order = orderValidationService.findByUid(orderUid);
        } catch (OrderException.OrderNotFound e) {
            return ConfirmReason.NOT_FOUND;
        }
        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            order.changeToExpired();
            return ConfirmReason.EXPIRED_ORDER;
        }
        if (confirmRequest.amount() != order.getTotal()) {
            order.changeToFailed();
            return ConfirmReason.TOTAL_MISMATCH;
        }
        if (order.isAnyOutOfStock()) {
            return ConfirmReason.OUT_OF_STOCK;
        }

        // 재고 예약
        for (var orderProduct : order.getProducts()) {
            var productId = orderProduct.getProduct().getId();
            var quantity = orderProduct.getQuantity();
            productStockService.reserveStock(productId, quantity); // 예약
        }

        // 결제 승인
        return ConfirmReason.CONFIRM;
    }

    // 웹훅이 결제 완료인지 판별
//    public void webhook(WebhookTransaction webhookTransaction) {
    @Transactional
    public void webhook(WebhookRequest webhookRequest) {
        /*
        - 결제가 승인되었을 때(모든 결제 수단) - (status : paid)
        - 가상계좌가 발급되었을 때 - (status : ready)
        - 가상계좌에 결제 금액이 입금되었을 때 - (status : paid)
        - 예약결제가 시도되었을 때 - (status : paid or failed)
        - 관리자 콘솔에서 결제 취소되었을 때 - (status : cancelled)
         */

        var order= orderValidationService.findByUid(UUID.fromString(webhookRequest.orderUid()));
        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            order.changeToExpired();
        }

        // 결제 상태 변경
        // 웹훅보다 브라우저의 처리가 먼저 되는 경우도 있음
        switch (webhookRequest.status()) {
            case "paid" : // committed
                order.changeToPaid();
//                notificationService.sendNotification(order.getMember().getId(), "결제 성공", "결제 성공입니다~");
                break;
            default :
                //
                for (var orderProduct : order.getProducts()) {
                    var productId = orderProduct.getProduct().getId();
                    var quantity = orderProduct.getQuantity();
                    productStockService.releaseStock(productId, quantity);
                }
                order.changeToFailed();
                break;
        }
    }
}
