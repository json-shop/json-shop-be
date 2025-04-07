package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.common.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final MemberRepository memberRepository; // import 추가
    private final DeliveryValidationService deliveryValidationService;


    public UUID deliveryReg(String email, DeliveryRegRequestDTO deliveryRegRequestDTO) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException());

        Delivery delivery = dto.toDelivery(member);

        if (!deliveryValidationService.validateZipcode(dto.zipCode())) {
            throw new DeliveryException.AddressNotFoundException(dto.zipCode());
        }

        deliveryRepository.save(delivery);

        return delivery.getUid();

    }

    public void deleteDelivery(String email, UUID uid) {
        Optional<Delivery> optionalDelivery = deliveryRepository.findByUid(uid);

        Delivery delivery = optionalDelivery.orElseThrow(() ->
                new DeliveryException.DeliveryNotFoundException(uid));

        if (!delivery.getMember().getEmail().equals(email)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }

        deliveryRepository.delete(delivery);
    }

    public void updateDelivery(String email, UUID uid, DeliveryRegRequestDTO dto) {
        Delivery delivery = deliveryRepository.findByUuid(uid).orElseThrow(()->new DeliveryException.DeliveryNotFoundException(uid));

        if (!delivery.getMember().getEmail().equals(email)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }

        //우편번호 유효성 검사
        if (!deliveryValidationService.validateZipcode(dto.zipCode())) {
            throw new DeliveryException.AddressNotFoundException(dto.zipCode());
        }

        delivery.setAddress(dto.address());
        delivery.setZipCode(dto.zipCode());
        delivery.setPhone(dto.phone());
        delivery.setRecipient(dto.recipient());
        deliveryRepository.save(delivery);

    }
}
