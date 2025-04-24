package deepdive.jsonstore.domain.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import deepdive.jsonstore.domain.product.dto.ProductOrderCountDTO;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;

import deepdive.jsonstore.domain.product.entity.Product;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import deepdive.jsonstore.domain.product.entity.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("SELECT p FROM Product p JOIN FETCH p.admin WHERE p.uid = :uid AND p.status != :status")
	Optional<Product> findByUidAndStatusIsNot(@Param("uid") UUID uid, @Param("status") ProductStatus status);

	@Query("SELECT p FROM Product p JOIN FETCH p.admin WHERE p.ulid = :ulid AND p.status != :status")
	Optional<Product> findByUlidAndStatusIsNot(@Param("ulid") byte[] ulid, @Param("status") ProductStatus status);

	@Query("SELECT p FROM Product p JOIN FETCH p.admin WHERE p.uid = :productUid")
	Optional<Product> findByUid(@Param("productUid") UUID productUid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "javax.persistence.lock.timeout", value = "1000"))
	@Query("SELECT p FROM Product p WHERE p.id = :id")
	Optional<Product> findWithLockById(@Param("id") Long id);

	@Query("SELECT new deepdive.jsonstore.domain.product.dto.ProductOrderCountDTO(op.product, "
		+ "COALESCE(SUM(CASE WHEN op.order.orderStatus = deepdive.jsonstore.domain.order.entity.OrderStatus.PAID "
		+ "THEN op.quantity ELSE 0 END), 0)) "
		+ "FROM OrderProduct op "
		+ "GROUP BY op.product.id ")
	List<ProductOrderCountDTO> findSoldCount();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "javax.persistence.lock.timeout", value = "1000"))
	@Query("SELECT p FROM Product p WHERE p.id IN :productIds")
	List<Product> findAllWithLockByIds(@Param("productIds") List<Long> productIds);

	@Query("SELECT p FROM Product p JOIN FETCH p.admin WHERE p.uid = :productUid and p.admin.uid = :adminUid")
	Optional<Product> findByUidAndAdminUid(@Param("productUid") UUID productUid, @Param("adminUid") UUID adminUid);
}
