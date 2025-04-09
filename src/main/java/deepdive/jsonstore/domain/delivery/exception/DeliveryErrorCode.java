package deepdive.jsonstore.domain.delivery.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode {

    //delivery
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.BAD_REQUEST, "배송지를 찾을 수 없습니다."),
    ZIPCODE_NOT_VALID(HttpStatus.BAD_REQUEST,"유효하지 않은 우편번호입니다."),
    ADDRESS_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"주소 검색 시스템 오류"),

    //entity
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 엔티티를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
