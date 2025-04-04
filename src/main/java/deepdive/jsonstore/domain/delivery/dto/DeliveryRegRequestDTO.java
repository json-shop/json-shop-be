package deepdive.jsonstore.domain.delivery.dto;

import deepdive.jsonstore.domain.delivery.entity.Delivery;

import java.util.UUID;

public record DeliveryRegRequestDTO(
        String address,
        String zipcode,
        String phone,
        String recipient) {

    public Delivery toDelivery(Member member) {
        return Delivery.builder()
                .address(address)
                .zipcode(zipcode)
                .phone(phone)
                .recipient(recipient)
                .uuid(UUID.randomUUID())
                .member(member)
                .build();
    }
}
