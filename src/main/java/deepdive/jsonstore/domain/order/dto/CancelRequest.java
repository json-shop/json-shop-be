package deepdive.jsonstore.domain.order.dto;

import lombok.Builder;

@Builder
public record CancelRequest(
       String cancelReason
){

}

