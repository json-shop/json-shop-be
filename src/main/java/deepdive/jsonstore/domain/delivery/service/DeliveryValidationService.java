package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryValidationService {

    private final DeliveryRepository deliveryRepository;

    public Delivery getDeliveryByUid(UUID uid) {
        return deliveryRepository.findByUid(uid).orElseThrow(DeliveryException.DeliveryNotFoundException::new);

    }

    public void validateMember(Delivery delivery, UUID memberUid) {
        if (!delivery.getMember().getUid().equals(memberUid)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }
    }

}