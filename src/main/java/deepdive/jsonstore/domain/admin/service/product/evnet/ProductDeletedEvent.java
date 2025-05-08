package deepdive.jsonstore.domain.admin.service.product.evnet;


import deepdive.jsonstore.domain.product.entity.Product;

public record ProductDeletedEvent(Product product) {
}
