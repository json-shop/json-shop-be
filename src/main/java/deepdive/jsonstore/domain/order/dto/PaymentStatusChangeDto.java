package deepdive.jsonstore.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

public record PaymentStatusChangeDto(
        String mId,
        String lastTransactionKey,
        String paymentKey,
        String orderId,
        String orderName,
        long taxExemptionAmount, //  면세
        String status,
        ZonedDateTime requestedAt,
        ZonedDateTime approvedAt,
        boolean useEscrow,
        boolean cultureExpense,
        String card,
        String virtualAccount,
        String transfer,
        String mobilePhone,
        String giftCertificate,
        String cashReceipt,
        String cashReceipts,
        String discount,
        String cancels,
        String secret,
        String type,
        Map<?,?>easyPay,
//                {
//      "provider": "토스페이",
//              "amount": 100,
//              "discountAmount": 0
//}
        String country,
        String failure,
        boolean isPartialCancelable,
        Map<?,?> receipt,
        Object checkout,
        String currency,
        long totalAmount,
        long balanceAmount,
        long suppliedAmount,
        long vat,
        long taxFreeAmount,
        String method,
        String version,
        String metadata
) {
}
