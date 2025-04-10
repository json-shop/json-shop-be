package deepdive.jsonstore.domain.notification.exception;

import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
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
}
