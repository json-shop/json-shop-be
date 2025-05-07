package deepdive.jsonstore.domain.delivery.controller;

import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.service.DeliveryService;
import deepdive.jsonstore.domain.delivery.service.DeliveryServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DeliveryControllerV2 {

    private final DeliveryServiceV2 deliveryService;

    //배송지 등록
    @PostMapping("/delivery")
    public ResponseEntity<?> createDelivery(@AuthenticationPrincipal(expression = "ulid") byte[] memberUlid, @RequestBody DeliveryRegRequestDTO deliveryRegDTO) {
        log.info("배송지 등록 요청: {}", Base64.getUrlEncoder().encodeToString(memberUlid));
        deliveryService.createDelivery(memberUlid, deliveryRegDTO);
        return ResponseEntity.created(URI.create("/api/v1/delivery")).build(); 
    }

    //배송지 삭제
    @DeleteMapping("/delivery/{ulid}")
    public ResponseEntity<?> deleteDelivery(@AuthenticationPrincipal(expression = "ulid") byte[] memberUlid, @PathVariable String ulid){
        log.info("배송지 삭제 요청: {}", Base64.getUrlEncoder().encodeToString(memberUlid));
        deliveryService.deleteDelivery(memberUlid, ulid);
        return ResponseEntity.ok().build();
    }

    //배송지 조회
    @GetMapping("/delivery")
    public ResponseEntity<?> getDelivery(@AuthenticationPrincipal(expression = "ulid") byte[] memberUlid){
        log.info("배송지 조회 요청: {}", Base64.getUrlEncoder().encodeToString(memberUlid));
        return ResponseEntity.ok(deliveryService.getDelivery(memberUlid));
  }

    //배송지 수정
    @PutMapping("/delivery/{ulid}")
    public ResponseEntity<?> updateDelivery(@AuthenticationPrincipal(expression = "ulid") byte[] memberUlid, @RequestBody DeliveryRegRequestDTO deliveryRegDTO, @PathVariable String ulid){
        log.info("배송지 수정 요청: {}", Base64.getUrlEncoder().encodeToString(memberUlid));
        deliveryService.updateDelivery(memberUlid, ulid, deliveryRegDTO);
        return ResponseEntity.ok(URI.create("/api/v2/delivery"));
    }

    //기본 배송지 등록
    @PatchMapping("/delivery/default/{deliveryUlid}")
    public ResponseEntity<?> setDefaultDelivery(@AuthenticationPrincipal(expression = "ulid") byte[] memberUlid, @PathVariable String deliveryUlid){
        log.info("배송지 기본 설정 요청: {}", Base64.getUrlEncoder().encodeToString(memberUlid));
        deliveryService.setDeliveryDefault(memberUlid, deliveryUlid);
        return ResponseEntity.noContent().build();
    }
}
