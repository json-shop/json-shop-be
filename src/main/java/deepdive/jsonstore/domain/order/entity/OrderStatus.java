package deepdive.jsonstore.domain.order.entity;

public enum OrderStatus {
    CREATED, // 생성
    PENDING_PAYMENT, // 결제 대기단계, 재고 점유 상태
    PAID,
    IN_DELIVERY,
    DONE,
    CANCELED,
    FAILED,
    EXPIRED // 결제 시간 초과시 실패시 만료
}
