package deepdive.jsonstore.domain.product.service;

import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.exception.ProductException;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductStockService {

    private final ProductRepository productRepository;

    // 리저브 스톡
    @Transactional
    public void consumeStock(Order order) {
        List<Long> productIds = order.getOrderProducts().stream()
                .map(op -> op.getProduct().getId())
                .toList();

        if (productIds == null || productIds.isEmpty()) return;

        List<Product> lockedProducts = productRepository.findAllWithLockByIds(productIds);

        Map<Long, Product> productMap = lockedProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderProduct op : order.getOrderProducts()) {
            Product product = productMap.get(op.getProduct().getId());
            product.decreaseStock(op.getQuantity());
        }
    }

    // 리저브 반환
    public void releaseStock(Order order) {
        List<Long> productIds = order.getOrderProducts().stream()
                .map(op -> op.getProduct().getId())
                .toList();

        if (productIds == null || productIds.isEmpty()) return;

        List<Product> lockedProducts = productRepository.findAllWithLockByIds(productIds);

        Map<Long, Product> productMap = lockedProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderProduct op : order.getOrderProducts()) {
            Product product = productMap.get(op.getProduct().getId());
            product.decreaseStock(op.getQuantity());
        }

        /* 상태가 재고없음인데 릴리즈된다면 상태 변경 */
    }
}
