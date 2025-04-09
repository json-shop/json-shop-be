package deepdive.jsonstore.domain.product.repository;

import java.util.Optional;
import java.util.UUID;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import deepdive.jsonstore.domain.product.entity.Product;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import deepdive.jsonstore.domain.product.entity.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("SELECT p FROM Product p JOIN FETCH p.admin WHERE p.uid = :uid AND p.status != :status")
	Optional<Product> findByUidAndStatusIsNot(@Param("uid") UUID uid, @Param("status") ProductStatus status);

	@Query("SELECT p FROM Product p JOIN FETCH p.admin WHERE p.uid = :productUid")
	Optional<Product> findByUid(@Param("productUid") UUID productUid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Product p WHERE p.id = :id")
	Optional<Product> findWithLockById(@Param("id") Long id);
}
