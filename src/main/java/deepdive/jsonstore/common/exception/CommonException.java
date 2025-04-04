package deepdive.jsonstore.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommonException extends RuntimeException {
    private final JsonStoreErrorCode errorCode;

    // 400
    public static class InvalidInputException extends CommonException {
        public InvalidInputException() {
            super(JsonStoreErrorCode.INVALID_INPUT_PARAMETER);
        }
    }

    // 500
    public static class InternalServerException extends CommonException {
        public InternalServerException() {
            super(JsonStoreErrorCode.SERVER_ERROR);
        }
    }
}