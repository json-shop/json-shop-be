package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.delivery.dto.DeliveryRegDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryValidationService deliveryValidationService;

    public void deliveryReg(DeliveryRegDTO deliveryRegDTO) {

        if (deliveryValidationService.validateZipcode(deliveryRegDTO.getZipcode())) {

        }
    }
}
