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
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_EXPIRED(HttpStatus.GONE, "만료된 주문입니다."),

    // Join (회원가입)
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    //delivery
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."),

    //entity
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 엔티티를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
