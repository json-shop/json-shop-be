package deepdive.jsonstore.domain.order.dto;

import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record OrderResponse(

        UUID orderUuid, // 주문번호
        UUID memberUuid, // 주문자
        String username,
        List<OrderProductResponse> products, // 주문할 상품
        OrderStatus orderStatus,
        // TODO : 딜리버리 추가
        //  DeliveryResponse delivery,
        String recipient,
        String phone,
        String address,
        String zipCode,
        int total // 총액

) {
    public static OrderResponse from(Order order) {
        var orderProductResponse = order.getProducts().stream().map(OrderProductResponse::from)
                .collect(Collectors.toList());
        return OrderResponse.builder()
                .orderUuid(order.getUuid())
                .memberUuid(order.getMember().getUuid())
                .orderStatus(order.getOrderStatus())
                .recipient(order.getRecipient())
                .phone(order.getPhone())
                .address(order.getAddress())
                .zipCode(order.getZipCode())
                .username(order.getMember().getUsername())
                .products(orderProductResponse)
                .total(order.getTotal())
                .build();
    }
}
