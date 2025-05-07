package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryValidationService {

    private final DeliveryRepository deliveryRepository;

    public Delivery getDeliveryByUid(UUID uid) {
        return deliveryRepository.findByUid(uid).orElseThrow(DeliveryException.DeliveryNotFoundException::new);
    }

    public Delivery getDeliveryByUlid(String ulid) {

        return deliveryRepository.findByUlid(Base64.getUrlDecoder().decode(ulid)).orElseThrow(DeliveryException.DeliveryNotFoundException::new);
    }

    public void validateMember(Delivery delivery, UUID memberUid) {
        if (!delivery.getMember().getUid().equals(memberUid)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }
    }

    public void validateMember(Delivery delivery, byte[] memberUlid) {
        if (!Arrays.equals(delivery.getMember().getUlid(), memberUlid)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }
    }

}
