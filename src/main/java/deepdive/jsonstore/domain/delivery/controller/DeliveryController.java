package deepdive.jsonstore.domain.delivery.controller;

import deepdive.jsonstore.domain.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @DeleteMapping("/delivery/{uid}")
    public ResponseEntity<?> deliveryDelete(String email, @PathVariable UUID uid){
            deliveryService.deleteDelivery(email, uid);
            return ResponseEntity.ok().build();

    }

}
