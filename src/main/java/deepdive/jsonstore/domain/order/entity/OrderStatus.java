package deepdive.jsonstore.domain.order.entity;

public enum OrderStatus {
    PENDING_PAYMENT, // 결제 대기단계 일정 시간 미 결제시
    PAID,
    IN_DELIVERY,
    DONE,
    CANCELED,
    EXPIRED // 결제 시간 초과시 실패시 만료
}
