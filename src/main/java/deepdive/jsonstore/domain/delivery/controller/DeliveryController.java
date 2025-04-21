package deepdive.jsonstore.domain.delivery.controller;

import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DeliveryController {

    private final DeliveryService deliveryService;

    //배송지 등록
    @PostMapping("/delivery")
    public ResponseEntity<?> createDelivery(@AuthenticationPrincipal(expression = "uid") UUID memberUid, @RequestBody DeliveryRegRequestDTO deliveryRegDTO) {
        log.info("배송지 등록 요청: {}", memberUid.toString());
        deliveryService.createDelivery(memberUid, deliveryRegDTO);
        return ResponseEntity.created(URI.create("/api/v1/delivery")).build(); 
    }

    //배송지 삭제
    @DeleteMapping("/delivery/{uid}")
    public ResponseEntity<?> deleteDelivery(@AuthenticationPrincipal(expression = "uid") UUID memberUid, @PathVariable UUID uid){
        log.info("배송지 삭제 요청: {}", memberUid.toString());
        deliveryService.deleteDelivery(memberUid, uid);
        return ResponseEntity.ok().build();
    }

    //배송지 조회
    @GetMapping("/delivery")
    public ResponseEntity<?> getDelivery(@AuthenticationPrincipal(expression = "uid") UUID memberUid){
        log.info("배송지 조회 요청: {}", memberUid.toString());
        return ResponseEntity.ok(deliveryService.getDelivery(memberUid));
  }

    //배송지 수정
    @PutMapping("/delivery/{uid}")
    public ResponseEntity<?> updateDelivery(@AuthenticationPrincipal(expression = "uid") UUID memberUid, @RequestBody DeliveryRegRequestDTO deliveryRegDTO, @PathVariable UUID uid){
        log.info("배송지 수정 요청: {}", memberUid.toString());
        deliveryService.updateDelivery(memberUid, uid, deliveryRegDTO);
        return ResponseEntity.ok(URI.create("/api/v1/delivery"));
    }

    //기본 배송지 등록
    @PatchMapping("/delivery/default/{deliveryUid}")
    public ResponseEntity<?> setDefaultDelivery(@AuthenticationPrincipal(expression = "uid") UUID memberUid, @PathVariable UUID deliveryUid){
        log.info("배송지 기본 등록 요청: {}", deliveryUid.toString());
        deliveryService.setDeliveryDefault(memberUid, deliveryUid);
        return ResponseEntity.noContent().build();
    }
}
