package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.delivery.service.DeliveryService;
import deepdive.jsonstore.domain.notification.entity.NotificationCategory;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final DeliveryService deliveryService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    /** 주문 엔티티를 uid로 조회 */
    @Transactional
    public Order loadByUid(UUID orderUid) {
        var foundedOrder = orderRepository.findByUid(orderUid)
                .orElseThrow(OrderException.OrderNotFound::new);
        if (foundedOrder.isExpired()) {
            foundedOrder.expire();
            orderRepository.save(foundedOrder);
        }
        return foundedOrder;
    }

    /**
     * 주문서 조회
     * @param orderUid 주문 uid
     * @return 주문서 Dto
     */
    public OrderResponse getOrderResponse(UUID orderUid) {
        var loadedOrder = loadByUid(orderUid);
        orderValidationService.validateExpiration(loadedOrder);
        return OrderResponse.from(loadedOrder);
    }

    /** Pagenated 주문서 목록 조회 */
    public Page<OrderResponse> getOrderResponsesByPage(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable)
                .map(OrderResponse::from);
    }


    /**
     * 재고를 확인하고 주문서를 생성합니다.
     *
     * @param memberId     주문자 아이디
     * @param orderRequest 주문 요청 Dto
     * @return 주문서 Dto
     */
    public UUID createOrder(Long memberId, OrderRequest orderRequest) {
        var member = memberValidationService.findById(memberId); // TODO : 리펙토링 필요
        List<OrderProduct> orderProducts = createOrderProducts(orderRequest);
        int total = calculateTotalAmount(orderProducts);

        // 주문 생성 및 저장
        Order order = Order.builder()
                .orderStatus(OrderStatus.CREATED)
                .member(member)
                .phone(orderRequest.phone())
                .recipient(orderRequest.recipient())
                .address(orderRequest.address())
                .zipCode(orderRequest.zipCode())
                .total(total)
                .build();

        // 주문 상품 등록
        orderProducts.forEach(order::addOrderProduct);
        var savedOrder = orderRepository.save(order);

        return savedOrder.getUid();
    }

    private int calculateTotalAmount(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .mapToInt(p -> p.getPrice() * p.getQuantity())
                .sum();
    }

    private List<OrderProduct> createOrderProducts(OrderRequest orderRequest) {
        List<OrderProduct> orderProducts = new ArrayList<>();
        List<String> outOfStockProducts = new ArrayList<>();
        for (OrderProductRequest orderProductReq : orderRequest.orderProductRequests()) {
            var product = productValidationService.findActiveProductById(orderProductReq.productUid());

            int quantity = orderProductReq.quantity();

            if (product.getStock() < quantity) {
                outOfStockProducts.add(product.getName());
            }

            orderProducts.add(OrderProduct.from(product, quantity));
        }
        if (!outOfStockProducts.isEmpty()) {
            throw new OrderException.OrderOutOfStockException(outOfStockProducts);
        }
        return orderProducts;
    }

    /**
     *
     * @param confirmRequest
     * @return 컨펌프로세스 결과를 반환합니다.
     */
    @Transactional
    public void confirmOrder(ConfirmRequest confirmRequest) {
        var orderUid = UUID.fromString(confirmRequest.orderId());
        var order = loadByUid(orderUid);


        // ------------트랜젝션---------------
        if (confirmRequest.amount() != order.getTotal()) {
            order.changeState(OrderStatus.FAILED);
            throw new OrderException.OrderTotalMismatchException();
        }

        // 재고 검사
        orderValidationService.validateProductStock(order);

        // 재고 예약
        // reserveStock(...) 호출 중 실패하면 트랜잭션이 롤백되긴 하지만
        // 재고 서비스가 외부 시스템이거나 자체 트랜잭션이라면 보상 트랜잭션 고려 필요
        // TODO : 에러 extra 수정
        order.getOrderProducts().forEach(op->
                productStockService.reserveStock(op.getProduct().getId(), op.getQuantity()));

        // 결제 대기 상태
        order.changeState(OrderStatus.PAYMENT_PENDING);
        // ------------------------------------

        // 주문 승인 및 결제키 등록
        var paymentResponse = paymentService.confirm(confirmRequest);
        var paymentKey = paymentResponse.get("paymentKey").toString();
        order.setPaymentKey(paymentKey);

        // ------------ 블로킹 ---------------------
        // 웹훅으로 비동기적으로 처리 가능


        // TODO: 트렌젝션 외부로
        var sb = new StringBuilder();
        var title = order.getTitle();
        sb.append(title).append("\n");
        sb.append(order.getTotal()).append("원 결제성공");
        var notificationBody = sb.toString();

        // 성공 알림 발송
        // TODO : 트렌젝션 외부로
        try {
            notificationService.sendNotification(order.getMember().getId(), "결제 성공", notificationBody, NotificationCategory.ORDERED);
        } catch (CommonException.InternalServerException e) {
            log.warn("발송 실패"); // 재발송 전략?
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
        var order = loadByUid(orderUid);

        // -------- 트랜젝션 ----------------------
        // 검증
        orderValidationService.validateBeforePayment(order);
        orderValidationService.validateExpiration(order);
        orderValidationService.validateBeforeShipping(order);

        // 전액 환불
        String reason = "사용자 요청";
        paymentService.cancelFullAmount(order.getPaymentKey(), reason);
        // ---------------------------------------

        // 알림 메시지 작성
        var sb = new StringBuilder();
        var title = order.getTitle();
        sb.append(title).append("\n");
        var notificationBody = sb.toString();

        order.getOrderProducts().forEach(op->
                productStockService.releaseStock(op.getProduct().getId(), op.getQuantity()));

        // 취소 성공 발송
        try {
            notificationService.sendNotification(order.getMember().getUid(), "결제 취소", notificationBody, NotificationCategory.CANCELED);
        } catch (Exception e) {
            log.info("발송 실패");
        }
        order.expire();
    }

    @Transactional
    public void updateOrderDeliveryBeforeShipping(UUID orderUid, UUID deliveryUid) {
        var order = loadByUid(orderUid);
        var delivery = deliveryService.getDeliveryByUid(deliveryUid);

        orderValidationService.validateExpiration(order);
        orderValidationService.validateBeforeShipping(order);

        order.updateDelivery(
                delivery.getAddress(),
                delivery.getZipCode(),
                delivery.getPhone(),
                delivery.getRecipient()
        );
    }
}
