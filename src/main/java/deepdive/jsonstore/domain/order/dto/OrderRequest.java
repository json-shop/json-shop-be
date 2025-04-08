package deepdive.jsonstore.domain.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderRequest(

        // 주문번호
        UUID orderUid,

        // 주문자
        UUID memberUid,

        // 주문할 상품
        List<OrderProductRequest> orderProductRequests,

        // 수령인
        String recipient,

        // 전화번호
        String phone,

        // 주소
        String address,

        // 우편번호
        String zipCode
) {
}
