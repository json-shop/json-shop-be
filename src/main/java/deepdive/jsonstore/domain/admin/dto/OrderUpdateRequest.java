package deepdive.jsonstore.domain.admin.dto;

import deepdive.jsonstore.domain.order.entity.OrderStatus;
import lombok.Builder;

@Builder
public record OrderUpdateRequest(
    OrderStatus status,
    String recipient, // 수령인
    String phone, // 전화번호
    String address, // 주소
    String zipCode // 우편번호
){

}
