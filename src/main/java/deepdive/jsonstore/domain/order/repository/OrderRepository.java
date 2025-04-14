package deepdive.jsonstore.domain.order.repository;

import deepdive.jsonstore.domain.order.dto.OrderResponse;
import deepdive.jsonstore.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUid(UUID uuid);

    Page<Order> findByMemberId(Long memberId, Pageable pageable);
}