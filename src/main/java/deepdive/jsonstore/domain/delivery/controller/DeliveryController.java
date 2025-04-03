package deepdive.jsonstore.domain.delivery.controller;

import deepdive.jsonstore.domain.delivery.service.DeliveryValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryValidationService deliveryValidationService;



    @GetMapping("/test")
    public String test() {
        deliveryValidationService.validateZipcode("03923");

        return "OK";
    }
}
