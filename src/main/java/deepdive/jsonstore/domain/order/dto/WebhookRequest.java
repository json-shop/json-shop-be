package deepdive.jsonstore.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebhookRequest (
        @JsonProperty("imp_uid")
        String imp_uid,
        @JsonProperty("merchant_uid")
        String orderUid,
        String status
) {
}
