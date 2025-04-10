package deepdive.jsonstore.common.exception;

import deepdive.jsonstore.domain.member.exception.MemberErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberException extends RuntimeException {
    private final MemberErrorCode errorCode;

    public static class CurrentPasswordIncorrectException extends MemberException {
        public CurrentPasswordIncorrectException() {super(MemberErrorCode.PASSWORD_MISMATCH);}
    }

    public static class PasswordMismatchException extends MemberException {
        public PasswordMismatchException() {super(MemberErrorCode.PASSWORD_MISMATCH);}
    }

    public static class AlreadyDeletedException extends MemberException {
        public AlreadyDeletedException() {
            super(MemberErrorCode.ALREADY_DELETED);
        }
    }


}
