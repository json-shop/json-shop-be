package deepdive.jsonstore.domain.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import deepdive.jsonstore.domain.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByUidAndActiveIsTrue(UUID uuid);

	Optional<Product> findByUid(UUID productUid);
}
