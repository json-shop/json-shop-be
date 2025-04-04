package deepdive.jsonstore.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JsonStoreErrorCode {

    //common
    INVALID_INPUT_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "시스템에 문제가 발생했습니다."),

    // order
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "타임아웃"),

    // Join (회원가입)
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
