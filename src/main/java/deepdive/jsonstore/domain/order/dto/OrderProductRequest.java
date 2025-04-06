package deepdive.jsonstore.domain.order.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderProductRequest(
        UUID productUuid,
        int quantity
){

}
