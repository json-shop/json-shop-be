package deepdive.jsonstore.common.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final JsonStoreErrorCode errorCode;

    public AuthException(JsonStoreErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 이메일/비밀번호 오류
    public static class InvalidCredentialsException extends AuthException {
        public InvalidCredentialsException() {
            super(JsonStoreErrorCode.INVALID_LOGIN_CREDENTIALS);
        }
    }

    public static class UserNotFoundException extends AuthException {
        public UserNotFoundException() {
            super(JsonStoreErrorCode.USER_NOT_FOUND);
        }
    }

    // 토큰 오류
    public static class InvalidTokenException extends AuthException {
        public InvalidTokenException() {
            super(JsonStoreErrorCode.INVALID_TOKEN);
        }
    }

    public static class ExpiredTokenException extends AuthException {
        public ExpiredTokenException() {
            super(JsonStoreErrorCode.EXPIRED_TOKEN);
        }
    }

    public static class UnsupportedTokenException extends AuthException {
        public UnsupportedTokenException() {
            super(JsonStoreErrorCode.UNSUPPORTED_TOKEN);
        }
    }

    public static class EmptyTokenException extends AuthException {
        public EmptyTokenException() {
            super(JsonStoreErrorCode.EMPTY_TOKEN);
        }
    }

    public static class AdminLoginFailedException extends AuthException {
        public AdminLoginFailedException() {
            super(JsonStoreErrorCode.ADMIN_LOGIN_FAILED);
        }
    }
}
