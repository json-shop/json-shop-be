package deepdive.jsonstore.domain.order.repository;

import deepdive.jsonstore.domain.order.dto.OrderResponse;
import deepdive.jsonstore.domain.order.entity.Order;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUid(UUID uid);

    Page<Order> findByUid(UUID uid, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.orderProducts op
            JOIN FETCH op.product p
            JOIN FETCH o.member
            WHERE o.uid = :uid
                AND p.status = 'ON_SALE'
	    """)
    Optional<Order> findWithLockByUid(@Param("uid") UUID uid);
}
