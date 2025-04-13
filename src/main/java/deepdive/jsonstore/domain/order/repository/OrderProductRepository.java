package deepdive.jsonstore.domain.order.repository;

import deepdive.jsonstore.domain.order.dto.OrderProductResponse;
import deepdive.jsonstore.domain.order.entity.OrderProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    Page<OrderProductResponse> findByAdminId(Long adminId, Pageable pageable);
}
