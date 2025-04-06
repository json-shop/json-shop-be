package deepdive.jsonstore.domain.delivery.repository;

import deepdive.jsonstore.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    boolean existsByUuid(UUID deliveryId);

    void deleteByUuid(UUID deliveryId);

    Optional<Delivery> findByUuid(UUID deliveryId);
}
