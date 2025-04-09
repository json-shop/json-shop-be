package deepdive.jsonstore.domain.order.dto;

import io.portone.sdk.server.PortOneClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ConfirmReason {
    CONFIRM(HttpStatus.OK, "성공"), // 200이면 무조건 성공
    NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "존재하지 않는 주문입니다."), // 프트원은 200이 아니면 500으로 검증 실패를 수신
    TOTAL_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "금액이 일치하지 않습니다."), // 프트원은 200이 아니면 500으로 검증 실패를 수신
    EXPIRED_ORDER(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 만료된 주문입니다."),
    OUT_OF_STOCK(HttpStatus.INTERNAL_SERVER_ERROR, "재고가 부족합니다."); // 프트원은 200이 아니면 500으로 검증 실패를 수신

    private final HttpStatus status;
    private final String reason;
}
