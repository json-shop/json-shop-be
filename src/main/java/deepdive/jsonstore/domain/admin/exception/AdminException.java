package deepdive.jsonstore.domain.admin.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AdminException extends RuntimeException {

	private final AdminErrorCode errorCode;

	public static class AdminNotFoundException extends AdminException {
		public AdminNotFoundException() {
			super(AdminErrorCode.ADMIN_NOT_FOUND);
		}
	}

	public static class AdminBadRequestException extends AdminException {
		public AdminBadRequestException() {
			super(AdminErrorCode.ADMIN_BAD_REQUEST);
		}
	}
}
