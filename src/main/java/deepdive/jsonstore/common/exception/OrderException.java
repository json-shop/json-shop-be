package deepdive.jsonstore.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderException extends RuntimeException {
    private final JsonStoreErrorCode errorCode;

    public static class TimeOutException extends OrderException {
        public TimeOutException(JsonStoreErrorCode errorCode) {
            super(errorCode);
        }
    }
}