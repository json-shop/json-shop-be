package deepdive.jsonstore.domain.delivery.dto;

public record DeliveryResponseDTO(
        String address,
        String zipCode,
        String phone,
        String recipient) {
}
