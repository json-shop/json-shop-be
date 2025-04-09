package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.dto.DeliveryResponseDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService{

    private final DeliveryRepository deliveryRepository;
    private final MemberRepository memberRepository; // import 추가
    private final DeliveryValidationService deliveryValidationService;


    public void createDelivery(String email, DeliveryRegRequestDTO dto) {
        Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);

        Delivery delivery = dto.toDelivery(member);

        if (!deliveryValidationService.validateZipCode(dto.zipCode())) {
            throw new DeliveryException.AddressNotFoundException();
        }

        deliveryRepository.save(delivery);

    }

    public void deleteDelivery(String email, UUID uid) {
        Delivery delivery = deliveryRepository.findByUid(uid).orElseThrow(DeliveryException.DeliveryNotFoundException::new);

        if (!delivery.getMember().getEmail().equals(email)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }

        deliveryRepository.delete(delivery);
    }

    public List<DeliveryResponseDTO> getDelivery(String email) {

        if (!memberRepository.existsByEmail(email)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }

        return deliveryRepository.findByMemberEmailAsDTO(email);

    }


    public void updateDelivery(String email, UUID uid, DeliveryRegRequestDTO dto) {
        Delivery delivery = deliveryRepository.findByUid(uid).orElseThrow(DeliveryException.DeliveryNotFoundException::new);

        if (!delivery.getMember().getEmail().equals(email)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }

        //우편번호 유효성 검사
        if (!deliveryValidationService.validateZipCode(dto.zipCode())) {
            throw new DeliveryException.AddressNotFoundException();
        }

        delivery.setAddress(dto.address());
        delivery.setZipCode(dto.zipCode());
        delivery.setPhone(dto.phone());
        delivery.setRecipient(dto.recipient());
        deliveryRepository.save(delivery);

    }

    public void setDeliveryDefault(String email, UUID uid) {
        Delivery delivery = deliveryRepository.findByUid(uid).orElseThrow(EntityNotFoundException::new);

        if (!delivery.getMember().getEmail().equals(email)) {
            throw new DeliveryException.DeliveryAccessDeniedException();
        }

        Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);

        member.setDefaultDelivery(delivery);
        memberRepository.save(member);

    }
}
