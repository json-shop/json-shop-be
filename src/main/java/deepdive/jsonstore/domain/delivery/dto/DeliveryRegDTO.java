package deepdive.jsonstore.domain.delivery.dto;

import deepdive.jsonstore.domain.delivery.entity.Delivery;
import lombok.Getter;

@Getter
public class DeliveryRegDTO {

    private String address;
    private String zipcode;
    private String phone;
    private String recipient;

    public Delivery toDelivery(){

    }

}