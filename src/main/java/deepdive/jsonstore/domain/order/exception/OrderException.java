package deepdive.jsonstore.domain.order.exception;

import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderException extends RuntimeException {
    private final OrderErrorCode errorCode;

    public static class OrderTimeOutException extends OrderException {
        public OrderTimeOutException() {
            super(OrderErrorCode.TIMEOUT);
        }
    }

    public static class OrderNotFound extends OrderException {
        public OrderNotFound() {
            super(OrderErrorCode.ORDER_NOT_FOUND);
        }
    }

    public static class OrderExpiredException extends OrderException {
        public OrderExpiredException() {
            super(OrderErrorCode.ORDER_EXPIRED);
        }
    }

    public static class OrderOutOfStockException extends OrderException {
        public OrderOutOfStockException() {
            super(OrderErrorCode.ORDER_OUT_OF_STOCK);
        }
    }

    public static class AlreadyInDeliveryException extends OrderException {
        public AlreadyInDeliveryException() {
            super(OrderErrorCode.ORDER_ALREADY_IN_DELIVERY);
        }
    }

    public static class NotPaidException extends OrderException {
        public NotPaidException() {
            super(OrderErrorCode.ORDER_NOT_PAID);
        }
    }

    public static class OrderTotalMismatchException extends OrderException {
        public OrderTotalMismatchException() {
            super(OrderErrorCode.ORDER_TOTAL_MISMATCH);
        }
    }
        public static class OrderCurrencyMismatchException extends OrderException {
        public OrderCurrencyMismatchException() {
            super(OrderErrorCode.ORDER_CURRENCY_MISMATCH);
        }
    }
}