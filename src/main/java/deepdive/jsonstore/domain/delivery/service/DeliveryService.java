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
public class DeliveryService{

    private final DeliveryRepository deliveryRepository;
    private final MemberValidationService memberValidationService;
    private final DeliveryValidationService deliveryValidationService;
    private final DeliveryAddressValidationService deliveryAddressValidationService;

    //배송지 등록
    public void createDelivery(UUID memberUid, DeliveryRegRequestDTO dto) {
        Member member = memberValidationService.findByUid(memberUid);
        Delivery delivery = dto.toDelivery(member);

        //우편번호 유효성 검사
        deliveryAddressValidationService.validateZipCode(dto.zipCode());

        deliveryRepository.save(delivery);

    }

    //배송지 삭제
    public void deleteDelivery(UUID memberUid, UUID deliveryUid) {
        Delivery delivery = deliveryValidationService.getDeliveryByUid(deliveryUid);

        //배송지 접근 권한 검사
        deliveryValidationService.validateMember(delivery,memberUid);

        deliveryRepository.delete(delivery);
    }

    //배송지 조회
    public List<DeliveryResponseDTO> getDelivery(UUID memberUid) {

        memberValidationService.existsByUid(memberUid);

        return deliveryRepository.findByMemberUidAsDTO(memberUid);

    }

    //배송지 수정
    @Transactional
    public void updateDelivery(UUID memberUid, UUID deliveryUid, DeliveryRegRequestDTO dto) {
        Delivery delivery = deliveryValidationService.getDeliveryByUid(deliveryUid);

        //배송지 접근 권한 검사
        deliveryValidationService.validateMember(delivery,memberUid);

        //우편번호 유효성 검사
        deliveryAddressValidationService.validateZipCode(dto.zipCode());

        delivery.updateDelivery(dto);

    }

    //기본 배송지 설정
    @Transactional
    public void setDeliveryDefault(UUID memberUid, UUID deliveryUid) {
        Delivery delivery = deliveryValidationService.getDeliveryByUid(deliveryUid);

        //배송지 접근 권한 검사
        deliveryValidationService.validateMember(delivery,memberUid);

        Member member = memberValidationService.findByUid(memberUid);

        member.setDefaultDelivery(delivery);
    }

    public Delivery getDeliveryByUid(UUID deliveryUid) {
        return deliveryValidationService.getDeliveryByUid(deliveryUid);
    }
}