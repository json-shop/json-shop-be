package deepdive.jsonstore.domain.order.repository;

import deepdive.jsonstore.domain.order.entity.OrderProduct;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query("SELECT op FROM OrderProduct op " +
            "JOIN FETCH op.product p " +
            "WHERE p.adminId = :adminId")
    Page<OrderProduct> findByProductAdminId(@Param("adminId")Long adminId, Pageable pageable);
}