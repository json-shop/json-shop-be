package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final MemberRepository memberRepository; // import 추가
    private final DeliveryValidationService deliveryValidationService;

    public Delivery deliveryReg(String email, DeliveryRegRequestDTO deliveryRegRequestDTO) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 이메일의 회원을 찾을 수 없습니다: " + email));

        Delivery delivery = deliveryRegRequestDTO.toDelivery(member);

        if (!deliveryValidationService.validateZipcode(deliveryRegRequestDTO.zipcode())) {
//        throw new InvalidZipcodeException("유효하지 않은 우편번호입니다: " + deliveryRegRequestDTO.zipcode());
        }

        return deliveryRepository.save(delivery);

    }
}
