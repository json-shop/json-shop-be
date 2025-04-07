package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.common.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService{

    private final DeliveryRepository deliveryRepository;

    public void deleteDelivery(String email, UUID uid) {
        Optional<Delivery> optionalDelivery = deliveryRepository.findByUuid(uid);

        Delivery delivery = optionalDelivery.orElseThrow(() ->
                new DeliveryException.DeliveryNotFoundException(uid));

        if (!delivery.getMember().getEmail().equals(email)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }

        deliveryRepository.delete(delivery);
    }
}
