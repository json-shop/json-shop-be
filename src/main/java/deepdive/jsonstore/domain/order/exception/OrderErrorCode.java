package deepdive.jsonstore.domain.order.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode {
    // order
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "타임아웃"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_EXPIRED(HttpStatus.GONE, "만료된 주문입니다."),
    ORDER_OUT_OF_STOCK(HttpStatus.NO_CONTENT, "상품 재고가 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
