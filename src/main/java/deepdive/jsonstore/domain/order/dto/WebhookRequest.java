package deepdive.jsonstore.domain.order.dto;

public record WebhookRequest(
        String createdAt,
        String eventType,
        PaymentStatusChangeDto data
) {
}
