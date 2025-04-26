package deepdive.jsonstore.domain.admin.service.product.evnet;

import deepdive.jsonstore.domain.product.entity.Product;
import lombok.Getter;

public record ProductUpdatedEvent(Product product) {

}
