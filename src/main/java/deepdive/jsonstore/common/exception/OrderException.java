package deepdive.jsonstore.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderException extends RuntimeException {
    private final JsonStoreErrorCode errorCode;

    public static class OrderTimeOutException extends OrderException {
        public OrderTimeOutException() {
            super(JsonStoreErrorCode.TIMEOUT);
        }
    }

    public static class OrderNotFound extends OrderException {
        public OrderNotFound() {
            super(JsonStoreErrorCode.ORDER_NOT_FOUND);
        }
    }

    public static class OrderExpiredException extends OrderException {
        public OrderExpiredException() {
            super(JsonStoreErrorCode.ORDER_EXPIRED);
        }
    }
}