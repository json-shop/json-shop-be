package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import io.portone.sdk.server.payment.PaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentClient paymentClient;

    public void cancelFullAmount(UUID paymentUid) {
        /* 포트원 결제 취소 API */
        // 전액 환불
        var paymentClient = new PaymentClient("","","");
        // CompletebleFuture : js의 프로미스 콜백같은것
        paymentClient.cancelPayment(
                "test", // 주문 uid
                null, // 금액
                null, // 면세 금액
                null, // vat 금액
                " 주문자 취소 요청 ("+ LocalDateTime.now()+")", // 취소 사유 및 일자 임시 저장
                null, // 요청자
                null, //promotionDiscontRetainOption
                null, //취소가능 금액
                null // 환불 계좌
        ).thenAccept((response)->{
            log.info("[주문취소] p_uid: "+ paymentUid);
        }).exceptionally((e) -> {
            log.info(e.getMessage());
            throw new CommonException.InternalServerException();
        });
    }

}
