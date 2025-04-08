package deepdive.jsonstore.domain.delivery.repository;

import deepdive.jsonstore.domain.delivery.dto.DeliveryResponseDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByUid(UUID uid);

    @Query("SELECT new deepdive.jsonstore.domain.delivery.dto.DeliveryResponseDTO(d.address, d.zipCode, d.phone, d.recipient) " +
            "FROM Delivery d " +
            "WHERE d.member.email = :email")
    List<DeliveryResponseDTO> findByMemberEmailAsDTO(@Param("email") String email);

}
