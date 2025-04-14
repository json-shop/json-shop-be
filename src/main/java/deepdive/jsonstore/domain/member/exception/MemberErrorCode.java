package deepdive.jsonstore.domain.member.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode {

    CurrentPasswordIncorrect(HttpStatus.BAD_REQUEST,"입력한 비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 탈퇴된 회원입니다."),
    DELETE_CONFLICT(HttpStatus.CONFLICT, "회원 탈퇴 중 문제가 발생했습니다."),

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST,"찾을 수 없는 회원입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
