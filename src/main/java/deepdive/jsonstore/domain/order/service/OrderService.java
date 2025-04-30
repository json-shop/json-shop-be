package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
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
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.service.ProductStockService;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutionException;

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
    private final MeterRegistry meterRegistry;

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

    /** 주문 엔티티를 uid로 조회 */
    @Transactional
    public Order loadByUid(byte[] orderUid) {
        var foundedOrder = orderRepository.findByUlid(orderUid)
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
        // todo : 본인것만 조회가능하지 않음
        var loadedOrder = loadByUid(orderUid);
        orderValidationService.validateExpiration(loadedOrder);
        return OrderResponse.from(loadedOrder);
    }

    /**
     * 주문서 조회
     * @param orderUlid 주문 uid
     * @return 주문서 Dto
     */
    public OrderResponse getOrderResponse(byte[] orderUlid) {
        var loadedOrder = loadByUid(orderUlid);
        orderValidationService.validateExpiration(loadedOrder);
        return OrderResponse.from(loadedOrder);
    }

    /** Pagenated 주문서 목록 조회 */
    public Page<OrderResponse> getOrderResponsesByPage(UUID memberUid, Pageable pageable) {
        var member = memberValidationService.findByUid(memberUid);
        return orderRepository.findByMemberId(member.getId(), pageable)
                .map(OrderResponse::from);
    }

    /** Pagenated 주문서 목록 조회 */
    public Page<OrderResponse> getOrderResponsesByPage(byte[] memberUid, Pageable pageable) {
        var member = memberValidationService.findByUlid(memberUid);
        return orderRepository.findByMemberId(member.getId(), pageable)
                .map(OrderResponse::from);
    }


    /**
     * 재고를 확인하고 주문서를 생성합니다.
     *
     * @param memberUid     주문자 UUID 아이디
     * @param orderRequest 주문 요청 Dto
     * @return 주문서 Dto
     */
    public UUID createOrder(UUID memberUid, OrderRequest orderRequest) {
        var member = memberValidationService.findByUid(memberUid);
        List<OrderProduct> orderProducts = createOrderProducts(orderRequest);
        int total = calculateTotalAmount(orderProducts);

        // 주문 생성 및 저장
        var order = Order.builder()
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

        meterRegistry.counter("business.order.created").increment();
        return savedOrder.getUid();
    }
    /**
     * 재고를 확인하고 주문서를 생성합니다.
     *
     * @param memberUid     주문자 ULID 아이디
     * @param orderRequestV2 주문 요청 Dto
     * @return 주문서 Dto
     */
    public byte[] createOrder(byte[] memberUid, OrderRequestV2 orderRequestV2) {
        var member = memberValidationService.findByUlid(memberUid);
        List<OrderProduct> orderProducts = createOrderProducts(orderRequestV2);
        int total = calculateTotalAmount(orderProducts);

        // 주문 생성 및 저장
        var order = Order.builder()
                .orderStatus(OrderStatus.CREATED)
                .member(member)
                .phone(orderRequestV2.phone())
                .recipient(orderRequestV2.recipient())
                .address(orderRequestV2.address())
                .zipCode(orderRequestV2.zipCode())
                .total(total)
                .build();

        // 주문 상품 등록
        orderProducts.forEach(order::addOrderProduct);
        var savedOrder = orderRepository.save(order);
        meterRegistry.counter("business.order.created").increment();
        return savedOrder.getUlid();
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

    private List<OrderProduct> createOrderProducts(OrderRequestV2 orderRequestV2) {
        List<OrderProduct> orderProducts = new ArrayList<>();
        List<String> outOfStockProducts = new ArrayList<>();
        for (OrderProductRequestV2 orderProductReq : orderRequestV2.orderProductRequests()) {
            var opUlidBytes = Base64.getUrlDecoder().decode(orderProductReq.productUlid());
            var product = productValidationService.findActiveProductById(opUlidBytes);

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
    @Retryable(
            value = { PessimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2.0, maxDelay = 2000)
    )
    @Transactional(timeout = 5)
    public void confirmOrder(ConfirmRequest confirmRequest) {
        var order = orderRepository.findWithLockByUid(UUID.fromString(confirmRequest.orderId().trim()))
                .orElseThrow(OrderException.OrderNotFound::new); // 여기서 order + orderProducts + product + member 모두 fetch + lock

        if (confirmRequest.amount() != order.getTotal()) {
            order.changeState(OrderStatus.FAILED);
            meterRegistry.counter("business.order.failed").increment();
            throw new OrderException.OrderTotalMismatchException();
        }

        // 재고 검사
//        orderValidationService.validateProductStock(order);
        orderValidationService.validateExpiration(order);
        
        // consume
        productStockService.consumeStock(order);

        order.changeState(OrderStatus.PAYMENT_PENDING);

        var paymentResponse = paymentService.confirm(confirmRequest);
//        paymentService.confirmTest(confirmRequest);
        order.setPaymentKey(paymentResponse.get("paymentKey").toString());

        var sb = new StringBuilder();
        var title = order.getTitle();
        sb.append(title).append("\n");
        sb.append(order.getTotal()).append("원 결제성공");
        var notificationBody = sb.toString();

        // 성공 알림 발송
        try {
            notificationService.sendNotification(
                    order.getMember().getUid(),
                    "결제 성공", notificationBody,
                    NotificationCategory.ORDERED);
        } catch (CommonException.InternalServerException e) {
            log.warn("발송 실패"); // 재발송 전략?
        }
    }

    /**
     *
     * @param confirmRequest
     * @return 컨펌프로세스 결과를 반환합니다.
     */
    @Retryable(
            value = { PessimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2.0, maxDelay = 2000)
    )
    @Transactional(timeout = 5)
    public void confirmOrderV2(ConfirmRequest confirmRequest) {

        var uild = Base64.getUrlDecoder().decode(confirmRequest.orderId());
        var order = orderRepository.findWithLockByUlid(uild)
                .orElseThrow(OrderException.OrderNotFound::new); // 여기서 order + orderProducts + product + member 모두 fetch + lock
        if (confirmRequest.amount() != order.getTotal()) {
            order.changeState(OrderStatus.FAILED);
            throw new OrderException.OrderTotalMismatchException();
        }

        // 재고 검사
//        orderValidationService.validateProductStock(order);
        orderValidationService.validateExpiration(order);

        // consume
        productStockService.consumeStock(order);

        order.changeState(OrderStatus.PAYMENT_PENDING);

        var paymentResponse = paymentService.confirm(confirmRequest);
//        paymentService.confirmTest(confirmRequest);
        order.setPaymentKey(paymentResponse.get("paymentKey").toString());
        log.info("LOG={}", paymentResponse);

        var sb = new StringBuilder();
        var title = order.getTitle();
        sb.append(title).append("\n");
        sb.append(order.getTotal()).append("원 결제성공");
        var notificationBody = sb.toString();

        // 성공 알림 발송
        try {
            notificationService.sendNotification(
                    order.getMember().getUid(),
                    "결제 성공", notificationBody,
                    NotificationCategory.ORDERED);
        } catch (CommonException.InternalServerException e) {
            log.warn("발송 실패"); // 재발송 전략?
        }
    }


    // 발송 전에 결제 취소 기능
    @Transactional
    public void cancelOrderBeforeShipment(UUID orderUid) {
        var order = loadByUid(orderUid);

        // 검증
        orderValidationService.validateBeforePayment(order);
        orderValidationService.validateExpiration(order);
        orderValidationService.validateBeforeShipping(order);

        // 전액 환불
        String reason = "사용자 요청";
        paymentService.cancelFullAmount(order.getPaymentKey(), reason);

        // 알림 메시지 작성
        var sb = new StringBuilder();
        var title = order.getTitle();
        sb.append(title).append("\n");
        var notificationBody = sb.toString();


        productStockService.releaseStock(order);

        // 취소 성공 발송
        try {
            notificationService.sendNotification(
                    order.getMember().getUid(),
                    "결제 취소", notificationBody,
                    NotificationCategory.CANCELED);
        } catch (Exception e) {
            log.info("발송 실패");
        }
        order.expire();

        meterRegistry.counter("business.order.cancelled").increment();
    }

    // 발송 전에 결제 취소 기능
    @Transactional
    public void cancelOrderBeforeShipment(byte[] orderUid) {
        var order = loadByUid(orderUid);

        // 검증
        orderValidationService.validateBeforePayment(order);
        orderValidationService.validateExpiration(order);
        orderValidationService.validateBeforeShipping(order);

        // 전액 환불
        String reason = "사용자 요청";
        paymentService.cancelFullAmount(order.getPaymentKey(), reason);

        // 알림 메시지 작성
        var sb = new StringBuilder();
        var title = order.getTitle();
        sb.append(title).append("\n");
        var notificationBody = sb.toString();


        productStockService.releaseStock(order);

        // 취소 성공 발송
        try {
            notificationService.sendNotification(
                    order.getMember().getUid(),
                    "결제 취소", notificationBody,
                    NotificationCategory.CANCELED);
        } catch (Exception e) {
            log.info("발송 실패");
        }
        order.expire();

        meterRegistry.counter("business.order.cancelled").increment();
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

    @Transactional
    public void updateOrderDeliveryBeforeShipping(byte[] orderUlid, byte[] deliveryUlid) {
        var order = loadByUid(orderUlid);
        //TODO : ULID
//        var delivery = deliveryService.getDeliveryByUlid(deliveryUlid);
        var delivery = new Delivery();

        orderValidationService.validateExpiration(order);
        orderValidationService.validateBeforeShipping(order);

        order.updateDelivery(
                delivery.getAddress(),
                delivery.getZipCode(),
                delivery.getPhone(),
                delivery.getRecipient()
        );
    }

    @Transactional
    public void webhook(WebhookRequest webhookRequest) {
        if (webhookRequest.eventType().equals("PAYMENT_STATUS_CHANGED")) {
            if (webhookRequest.data().status().equals("DONE")) {
                var orderUlid = Base64.getUrlDecoder().decode(webhookRequest.data().orderId());
                var order = loadByUid(orderUlid);
                order.changeState(OrderStatus.PAID);
            }
        }
    }
}
