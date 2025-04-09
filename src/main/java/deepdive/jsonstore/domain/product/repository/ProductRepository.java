package deepdive.jsonstore.domain.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByUidAndStatusIsNot(UUID uuid, ProductStatus status);

	Optional<Product> findByUid(UUID productUid);
}
