package deepdive.jsonstore.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JoinException extends RuntimeException {
    private final JsonStoreErrorCode errorCode;

    public static class DuplicateEmailException extends JoinException {
        public DuplicateEmailException(JsonStoreErrorCode errorCode) {
            super(errorCode);
        }
    }

    public static class PasswordMismatchException extends JoinException {
        public PasswordMismatchException(JsonStoreErrorCode errorCode) {
            super(errorCode);
        }
    }
}
