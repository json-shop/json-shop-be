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

    public static class MemberLoginFailedException extends AuthException {
        public MemberLoginFailedException() {
            super(JsonStoreErrorCode.MEMBER_LOGIN_FAILED);
        }
    }


    // 인증되지 않은 사용자 (로그인하지 않음)
    public static class UnauthenticatedAccessException extends AuthException {
        public UnauthenticatedAccessException() {
            super(JsonStoreErrorCode.UNAUTHENTICATED_ACCESS);
        }
    }

    // 권한 부족한 사용자 (로그인했지만 접근 권한 없음)
    public static class AccessDeniedException extends AuthException {
        public AccessDeniedException() {
            super(JsonStoreErrorCode.ACCESS_DENIED);
        }
    }

    //  다른 사용자 리소스 접근 시
    public static class ForbiddenAccessException extends AuthException {
        public ForbiddenAccessException() {
            super(JsonStoreErrorCode.FORBIDDEN_ACCESS);
        }
    }

}
