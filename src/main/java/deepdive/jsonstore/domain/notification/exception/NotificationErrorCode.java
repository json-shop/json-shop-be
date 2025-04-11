package deepdive.jsonstore.domain.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode {

	MISSING_FCM_TOKEN(HttpStatus.BAD_REQUEST, "FCM 토큰이 없습니다."),
	NOTIFICATION_MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
