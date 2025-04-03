package deepdive.jsonstore.domain.delivery.mapper;

import deepdive.jsonstore.domain.delivery.dto.DeliveryRegDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;

public class DeliveryMapper {
    public static Delivery toDelivery(DeliveryRegDTO deliveryRegDTO) {
        return Delivery.builder()

                .build();

    }
}
