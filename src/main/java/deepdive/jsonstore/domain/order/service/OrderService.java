package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.order.exception.OrderException;
import deepdive.jsonstore.domain.member.service.MemberValidationService;
import deepdive.jsonstore.domain.notification.service.NotificationService;
import deepdive.jsonstore.domain.order.dto.*;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import deepdive.jsonstore.domain.product.service.ProductStockService;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import io.portone.sdk.server.payment.PaymentClient;
import io.portone.sdk.server.webhook.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductValidationService productValidationService;
    private final ProductStockService productStockService;
    private final OrderValidationService orderValidationService;
    private final MemberValidationService memberValidationService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    @Value("${order.expire-minutes}") private int ORDER_EXPIRE_TIME;
    @Value("${portone.webhook.secret-key}") private String key;

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
    public UUID createOrder(Long memberId, OrderRequest orderRequest) {

        var member = memberValidationService.findById(memberId);
        List<OrderProduct> orderProducts = new ArrayList<>();
        int total = 0;

        for (OrderProductRequest orderProductReq : orderRequest.orderProductRequests()) {
            var product = productValidationService.findActiveProductById(orderProductReq.productUid());

            int quantity = orderProductReq.quantity();
            int price = product.getPrice();

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
                .orderStatus(OrderStatus.CREATED)
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

    /**
     *
     * @param webhookTransactionDataConfirm
     * @return 컨펌프로세스 결과를 반환합니다.
     */
    @Transactional
    public ConfirmReason confirmOrder(WebhookTransactionDataConfirm webhookTransactionDataConfirm) {

        log.info("accepted");

        /* new WebhookVerifier() */

        var orderUid = UUID.fromString(webhookTransactionDataConfirm.getPaymentId());
        Order order;
        try {
           order = orderValidationService.findByUid(orderUid);
        } catch (OrderException.OrderNotFound e) {
            return ConfirmReason.NOT_FOUND;
        }
        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            order.changeState(OrderStatus.EXPIRED);
            return ConfirmReason.EXPIRED_ORDER;
        }
        if (webhookTransactionDataConfirm.getTotalAmount() != order.getTotal()) {
            order.changeState(OrderStatus.FAILED);
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

        order.changeState(OrderStatus.PAYMENT_PENDING);

        // 결제 승인
        return ConfirmReason.CONFIRM;
    }

    // 웹훅이 결제 완료인지 판별
    @Transactional
    public void webhook(WebhookTransactionData webhookTransactionData) {

        var orderUid = UUID.fromString(webhookTransactionData.getPaymentId());
        var order= orderValidationService.findByUid(orderUid);

        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            order.expire();
        }

        // 결제 상태 변경
        // 웹훅보다 브라우저의 처리가 먼저 되는 경우도 있음
        if (webhookTransactionData instanceof WebhookTransactionDataPaid) {
            order.changeState(OrderStatus.PAID);
            try {
                notificationService.sendNotification(order.getMember().getId(), "결제 성공", "결제 성공입니다~");
            } catch (Exception e) {
                log.info("발송 실패");
            }
        } else if (webhookTransactionData instanceof WebhookTransactionDataConfirm){
            System.out.println("컨펌이여유?");
        } else {
            // 재고 릴리즈
            for (var orderProduct : order.getProducts()) {
                var productId = orderProduct.getProduct().getId();
                var quantity = orderProduct.getQuantity();
                productStockService.releaseStock(productId, quantity);
            }
            order.changeState(OrderStatus.FAILED);
        }
    }

    // 발송 전에 결제 취소 기능
    @Transactional
    public void cancelOrderBeforeShipment(UUID orderUid) {
        var order = orderValidationService.findByUid(orderUid);
        var currentStatus = order.getOrderStatus();

        // 결제 전 주문
        if (currentStatus.ordinal() <= OrderStatus.PAYMENT_PENDING.ordinal()) {
            throw new OrderException.NotPaidException();
        }

        // 만료된 주문
        if (currentStatus.ordinal() >= OrderStatus.CANCELLED.ordinal()) {
            throw new OrderException.OrderExpiredException();
        }

        // 이미 배송 중
        if (currentStatus == OrderStatus.IN_DELIVERY) {
            throw new OrderException.AlreadyInDeliveryException();
        }

        // 전액 환불
        paymentService.cancelFullAmount(orderUid);
        // 취소 발송
        try {
            notificationService.sendNotification(order.getMember().getId(), "결제 취소", "결제 취소입니다~");
        } catch (Exception e) {
            log.info("발송 실패");
        }
        order.expire();
    }
}
