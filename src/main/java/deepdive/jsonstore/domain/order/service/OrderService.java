package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.OrderException;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.order.dto.OrderProductRequest;
import deepdive.jsonstore.domain.order.dto.OrderRequest;
import deepdive.jsonstore.domain.order.dto.OrderResponse;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductValidationService productValidationService;
    private final OrderValidationService orderValidationService;
//    private final MemberValidationService memberValidationService;

    /**
     * 주문서 조회
     * @param orderUid 주문 uid
     * @return 주문서 Dto
     */
    public OrderResponse getOrder(UUID orderUid) {

        // 주문서 조회
        var foundOrder= orderValidationService.getByUuid(orderUid);

        // 주문서 만료 처리
        if (foundOrder.getExpiredAt().isAfter(LocalDateTime.now())) {
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
     * @param memberId 주문자 아이디
     * @param orderRequest 주문 요청 Dto
     * @return 주문서 Dto
     */
    public OrderResponse createOrder(Long memberId, OrderRequest orderRequest) {

//        var member = memberValidationService.findByUuid(memberId);
        var member = Member.builder().build();

        List<OrderProduct> orderProducts = new ArrayList<>();
        int total = 0;

        for (OrderProductRequest orderProductReq : orderRequest.orderProductRequests()) {
            var product = productValidationService.findActiveProductById(orderProductReq.productUuid());

            int quantity = orderProductReq.quantity();
            int price = product.getPrice();

            // 재고 1차 검증(2차는 승인 때)
            // productService.checkStock(product);
            // TODO : 먼저 검증 반복문 추가할 것
            // TODO : 재고가 부족한 목록을 에러로 반환할 것. 익셉션에 T extra 추가
            if (product.getStock() < quantity) {
                throw new IllegalStateException("상품 재고가 부족합니다.");
            }

            // 리스트에 추가
            orderProducts.add(OrderProduct.from(product, quantity));

            // 총액 계산
            total += price * quantity;
        }

        Order order = Order.builder()
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .member(member)
                .phone(orderRequest.phone())
                .recipient(orderRequest.recipient())
                .address(orderRequest.address())
                .zipCode(orderRequest.zipCode())
                .products(orderProducts)
                .total(total)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();

        var savedOrder = orderRepository.save(order);

        return OrderResponse.from(savedOrder);
    }
}
