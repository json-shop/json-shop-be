package deepdive.jsonstore.domain.order.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderRequestV2(

        List<OrderProductRequestV2> orderProductRequests, // 주문할 상품
        String recipient, // 수령인
        String phone, // 전화번호
        String address, // 주소
        String zipCode // 우편번호
) {
}
