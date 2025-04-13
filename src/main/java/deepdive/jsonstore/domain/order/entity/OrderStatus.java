package deepdive.jsonstore.domain.order.entity;

public enum OrderStatus {
    CREATED, // 생성
    PAYMENT_PENDING, // 결제 대기단계, 재고 점유 상태
    PAID,
    PREPARING_SHIPMENT,
    IN_DELIVERY,
    DONE, // 배송완료
    CANCELED, // 주문 취소
    FAILED, // 위변조
    EXPIRED // 만료된 주문(사용자취소, 시간 만료),
}
