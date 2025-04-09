package deepdive.jsonstore.domain.product.repository;

import java.util.Optional;
import java.util.UUID;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import deepdive.jsonstore.domain.product.entity.Product;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByUidAndActiveIsTrue(UUID uuid);

	Optional<Product> findByUid(UUID productUid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Product p WHERE p.id = :id")
	Optional<Product> findWithLockById(@Param("id") Long id);
}
