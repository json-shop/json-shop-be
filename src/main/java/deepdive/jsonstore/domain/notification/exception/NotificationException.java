package deepdive.jsonstore.domain.notification.exception;

import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationException extends RuntimeException {

	private final NotificationErrorCode errorCode;

	public static class MissingFcmTokenException extends NotificationException {
		public MissingFcmTokenException() {
			super(NotificationErrorCode.MISSING_FCM_TOKEN);
		}
	}

	public static class NotificationMemberNotFoundException extends NotificationException {
		public NotificationMemberNotFoundException() {
			super(NotificationErrorCode.NOTIFICATION_MEMBER_NOT_FOUND);
		}
	}
}
