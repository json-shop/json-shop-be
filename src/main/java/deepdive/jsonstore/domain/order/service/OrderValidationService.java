package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.order.entity.OrderStatus;
import deepdive.jsonstore.domain.order.exception.OrderException;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderValidationService {

    private final ProductValidationService productValidationService;


    @Transactional
    public void validateExpiration(Order order) {
        if (order.getOrderStatus().ordinal() >= OrderStatus.CANCELED.ordinal()) {
            throw new OrderException.OrderExpiredException();
        }
    }

    public void validateProductStock(Order order) {
        List<String> outOfStockProducts = new ArrayList<>();
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            var product = productValidationService.findActiveProductById(orderProduct.getProduct().getUid());
            if (product.getStock() < orderProduct.getQuantity()) {
                outOfStockProducts.add(product.getName());
            }
        }
        if (!outOfStockProducts.isEmpty()) {
            throw new OrderException.OrderOutOfStockException(outOfStockProducts);
        }
    }

    public void validateBeforeShipping (Order order) {
        if (order.getOrderStatus().ordinal() >= OrderStatus.IN_DELIVERY.ordinal()) {
            throw new OrderException.AlreadyStartDeliveryException();
        }
    }

    public void validateBeforePayment(Order order) {
        if (order.getOrderStatus().ordinal() <= OrderStatus.PAYMENT_PENDING.ordinal()) {
            throw new OrderException.NotPaidException();
        }
    }
}
