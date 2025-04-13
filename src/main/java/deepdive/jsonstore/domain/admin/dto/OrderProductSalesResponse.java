package deepdive.jsonstore.domain.admin.dto;

import deepdive.jsonstore.domain.order.entity.OrderProduct;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrderProductSalesResponse(
        LocalDateTime orderedAt,
        UUID orderUid,
        UUID productUid,
        String productName,
        String productImageUrl,
        int quantity,
        int amount,
        int subTotal
){
    public static OrderProductSalesResponse from(OrderProduct orderProduct) {
        int quantity = orderProduct.getQuantity();
        int amount = orderProduct.getPrice();
        int subTotal =  quantity * amount;
        return OrderProductSalesResponse.builder()
                .orderedAt(orderProduct.getCreatedAt())
                .orderUid(orderProduct.getOrder().getUid())
                .productUid(orderProduct.getUid())
                .productName(orderProduct.getProduct().getName())
                .productImageUrl(orderProduct.getProduct().getImage())
                .quantity(quantity)
                .amount(amount)
                .subTotal(subTotal)
                .build();
    }
}
