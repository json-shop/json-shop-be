package deepdive.jsonstore.domain.delivery.repository;

import deepdive.jsonstore.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

}
