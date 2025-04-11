package deepdive.jsonstore.domain.order.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode {
    // order
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "시간초과"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_EXPIRED(HttpStatus.GONE, "만료된 주문입니다."),
    ORDER_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "상품 재고가 없습니다."),
    ORDER_ALREADY_IN_DELIVERY(HttpStatus.BAD_REQUEST, "이미 배송 중 입니다."),
    ORDER_NOT_PAID(HttpStatus.BAD_REQUEST, "결제하지 않은 주문입니다."),
    ORDER_TOTAL_MISMATCH(HttpStatus.BAD_REQUEST, "결제 총액이 맞지 않습니다."),
    ORDER_CURRENCY_MISMATCH(HttpStatus.BAD_REQUEST, "통화가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
