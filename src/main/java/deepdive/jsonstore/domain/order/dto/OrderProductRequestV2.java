package deepdive.jsonstore.domain.order.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderProductRequestV2(
        String productUlid,
        int quantity
){

}
