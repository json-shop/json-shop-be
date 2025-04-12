package deepdive.jsonstore.domain.order.dto;

import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record OrderResponse(

        UUID orderUid, // 주문번호
        UUID memberUid, // 주문자
        String username,
        List<OrderProductResponse> orderProducts, // 주문할 상품
        OrderStatus orderStatus,
        String recipient,
        String phone,
        String address,
        String zipCode,
        int total // 총액

) {
    public static OrderResponse from(Order order) {
        var orderProductResponse = order.getOrderProducts().stream().map(OrderProductResponse::from)
                .collect(Collectors.toList());
        return OrderResponse.builder()
                .orderUid(order.getUid())
                .memberUid(order.getMember().getUid())
                .orderStatus(order.getOrderStatus())
                .recipient(order.getRecipient())
                .phone(order.getPhone())
                .address(order.getAddress())
                .zipCode(order.getZipCode())
                .username(order.getMember().getUsername())
                .orderProducts(orderProductResponse)
                .total(order.getTotal())
                .build();
    }
}
