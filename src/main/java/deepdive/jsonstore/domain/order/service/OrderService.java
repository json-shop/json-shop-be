package deepdive.jsonstore.domain.order.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
                .total(total)
                .expiredAt(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_TIME))
                .build();

        // 주문 상품 등록
        // TODO : 리펙토링?
        orderProducts.forEach(order::addProduct);
        var savedOrder = orderRepository.save(order);

        return savedOrder.getUid();
    }

    /**
     *
     * @param confirmRequest
     * @return 컨펌프로세스 결과를 반환합니다.
     */
    @Transactional
    public void confirmOrder(ConfirmRequest confirmRequest) {
        var orderUid = UUID.fromString(confirmRequest.orderId());
        var order = orderValidationService.findByUid(orderUid);

        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            order.changeState(OrderStatus.EXPIRED);
            throw new OrderException.OrderExpiredException();
        }
//        if () {
//            /* 통화 검사 */
//        }

        if (confirmRequest.amount() != order.getTotal()) {
            order.changeState(OrderStatus.FAILED);
            //TODO : 실패시 리디렉션?
            throw new OrderException.OrderTotalMismatchException();
        }
        if (order.isAnyOutOfStock()) {
            throw new OrderException.OrderOutOfStockException();
        }

        // 재고 예약
        for (var orderProduct : order.getProducts()) {
            var productId = orderProduct.getProduct().getId();
            var quantity = orderProduct.getQuantity();
            productStockService.reserveStock(productId, quantity); // 예약
        }

        // 결제 대기 상태
        // order.changeState(OrderStatus.PAYMENT_PENDING);

        // 주문 승인 및 결제키 등록
        var paymentResponse = paymentService.confirm(confirmRequest);
        var paymentKey = paymentResponse.get("paymentKey").toString();
        order.setPaymentKey(paymentKey);

        var sb = new StringBuilder();
        var title = order.getTitle();

        sb.append(title).append("\n");
        sb.append(order.getTotal()).append("원 결제성공");
        var notificationBody = sb.toString();

        // 성공 알림 발송
        try {
            notificationService.sendNotification(order.getMember().getId(), "결제 성공", notificationBody);
        } catch (Exception e) {
            // 재발송 전략?
            log.info("발송 실패");
        }

        // 주문 상태 "결제"로 변경
        order.changeState(OrderStatus.PAID);

    }

    // 웹훅이 결제 완료인지 판별
    @Transactional
    public void webhook() {
        // 주문 완료
        // 주문 실패
        // 주문 취소
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
        String reason = "사용자 요청";
        paymentService.cancelFullAmount(order.getPaymentKey(), reason);


        // 알림 메시지 작성
        var sb = new StringBuilder();
        var title = order.getTitle();

        sb.append(title).append("\n");
        var notificationBody = sb.toString();

        // 취소 성공 발송
        try {
            notificationService.sendNotification(order.getMember().getId(), "결제 취소", notificationBody);
        } catch (Exception e) {
            log.info("발송 실패");
        }
        order.expire();
    }
}
