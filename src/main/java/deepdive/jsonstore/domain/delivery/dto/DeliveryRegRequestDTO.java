package deepdive.jsonstore.domain.delivery.dto;

import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.member.entity.Member;

import java.util.UUID;

public record DeliveryRegRequestDTO(
        String address,
        String zipCode,
        String phone,
        String recipient) {

    public Delivery toDelivery(Member member) {
        return Delivery.builder()
                .address(address)
                .zipCode(zipCode)
                .phone(phone)
                .recipient(recipient)
                .uuid(UUID.randomUUID())
                .member(member)
                .build();
    }
}
