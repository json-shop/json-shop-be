package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.common.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.dto.DeliveryResponseDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.member.model.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 이메일의 회원을 찾을 수 없습니다: " + email));

        Delivery delivery = deliveryRegRequestDTO.toDelivery(member);

        if (!deliveryValidationService.validateZipcode(deliveryRegRequestDTO.zipCode())) {
//        throw new InvalidZipcodeException("유효하지 않은 우편번호입니다: " + deliveryRegRequestDTO.zipcode());
        }

        deliveryRepository.save(delivery);

        return delivery.getUid();

    }

    public void deleteDelivery(String email, UUID uid) {
        Optional<Delivery> optionalDelivery = deliveryRepository.findByUuid(uid);

        Delivery delivery = optionalDelivery.orElseThrow(() ->
                new DeliveryException.DeliveryNotFoundException(uid));

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
}