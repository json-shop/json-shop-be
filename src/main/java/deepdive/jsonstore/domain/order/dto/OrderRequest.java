package deepdive.jsonstore.domain.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderRequest(

        List<OrderProductRequest> orderProductRequests, // 주문할 상품
        String recipient, // 수령인
        String phone, // 전화번호
        String address, // 주소
        String zipCode // 우편번호
) {
}
