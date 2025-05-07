package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.dto.DeliveryResponseDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.service.MemberValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceV2 {

    private final DeliveryRepository deliveryRepository;
    private final MemberValidationService memberValidationService;
    private final DeliveryValidationService deliveryValidationService;
    private final DeliveryAddressValidationService deliveryAddressValidationService;

    //배송지 등록
    public void createDelivery(byte[] memberUid, DeliveryRegRequestDTO dto) {
        Member member = memberValidationService.findByUlid(memberUid);
        Delivery delivery = dto.toDelivery(member);

        //우편번호 유효성 검사
        deliveryAddressValidationService.validateZipCode(dto.zipCode());

        deliveryRepository.save(delivery);

    }

    //배송지 삭제
    public void deleteDelivery(byte[] memberUlid, String deliveryUlid) {
        Delivery delivery = deliveryValidationService.getDeliveryByUlid(deliveryUlid);

        //배송지 접근 권한 검사
        deliveryValidationService.validateMember(delivery,memberUlid);

        deliveryRepository.delete(delivery);
        log.info("배송지 삭제 완료: {}", delivery.getUlid().toString());
    }

    //배송지 조회
    public List<DeliveryResponseDTO> getDelivery(byte[] memberUid) {

        memberValidationService.existsByUlid(memberUid);

        return deliveryRepository.findByMemberUlidAsDTO(memberUid);

    }

    //배송지 수정
    @Transactional
    public void updateDelivery(byte[] memberUlid, String deliveryUlid, DeliveryRegRequestDTO dto) {
        Delivery delivery = deliveryValidationService.getDeliveryByUlid(deliveryUlid);

        //배송지 접근 권한 검사
        deliveryValidationService.validateMember(delivery,memberUlid);

        //우편번호 유효성 검사
        deliveryAddressValidationService.validateZipCode(dto.zipCode());

        delivery.updateDelivery(dto);

    }

    //기본 배송지 설정
    @Transactional
    public void setDeliveryDefault(byte[] memberUid, String deliveryUlid) {
        Delivery delivery = deliveryValidationService.getDeliveryByUlid(deliveryUlid);

        //배송지 접근 권한 검사
        deliveryValidationService.validateMember(delivery,memberUid);

        Member member = memberValidationService.findByUlid(memberUid);

        member.setDefaultDelivery(delivery);
    }

}
