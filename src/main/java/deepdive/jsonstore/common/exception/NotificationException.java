package deepdive.jsonstore.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationException extends RuntimeException {
    private final JsonStoreErrorCode errorCode;

    public static class MissingFcmTokenException extends NotificationException {
        public MissingFcmTokenException() {
            super(JsonStoreErrorCode.MISSING_FCM_TOKEN);
        }
    }

    public static class NotificationMemberNotFoundException extends NotificationException {
        public NotificationMemberNotFoundException() {
            super(JsonStoreErrorCode.NOTIFICATION_MEMBER_NOT_FOUND);
        }
    }

    public static class RedisServerException extends NotificationException {
        public RedisServerException() {
            super(JsonStoreErrorCode.REDIS_SERVER_ERROR);
        }
    }
}