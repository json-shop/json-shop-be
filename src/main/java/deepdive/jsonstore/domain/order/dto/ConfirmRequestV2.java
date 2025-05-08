package deepdive.jsonstore.domain.order.dto;

import lombok.Builder;

//public record ConfirmRequest (
//     String type, //Transaction.Confirm 고정
//     LocalDateTime timestamp,
//     ConfirmDataRequest data
//){
//
//}

@Builder
public record ConfirmRequestV2(
       String paymentKey,
       String orderId,
       Long amount
){

}

