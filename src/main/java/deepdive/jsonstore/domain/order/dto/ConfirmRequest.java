package deepdive.jsonstore.domain.order.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

//public record ConfirmRequest (
//     String type, //Transaction.Confirm 고정
//     LocalDateTime timestamp,
//     ConfirmDataRequest data
//){
//
//}

@Builder
public record ConfirmRequest (
       String paymentKey,
       String orderId,
       Long amount
){

}

