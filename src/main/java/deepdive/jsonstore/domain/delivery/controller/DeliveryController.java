package deepdive.jsonstore.domain.delivery.controller;

import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.service.DeliveryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/delivery")
    public ResponseEntity<?> deliveryReg(String email, @RequestBody DeliveryRegRequestDTO deliveryRegDTO) { //인증 모듈 추가 시 수정 필요 /예외처리 개선 필요
        try{
            UUID uuid = deliveryService.deliveryReg(email, deliveryRegDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(uuid);
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }
    @DeleteMapping("/delivery/{uid}")
    public ResponseEntity<?> deliveryDelete(String email, @PathVariable UUID uid){
        deliveryService.deleteDelivery(email, uid);
        return ResponseEntity.ok().build();

    }

    @GetMapping("/delivery")
    public ResponseEntity<?> getDelivery(String email){

        return ResponseEntity.ok(deliveryService.getDelivery(email));
    }

}
