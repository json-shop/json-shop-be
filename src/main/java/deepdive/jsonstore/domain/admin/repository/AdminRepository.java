package deepdive.jsonstore.domain.admin.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import deepdive.jsonstore.domain.admin.entity.Admin;
import io.lettuce.core.dynamic.annotation.Param;

public interface AdminRepository extends JpaRepository<Admin, Long> {

	@Query("SELECT a FROM Admin a WHERE a.uid = :uid AND a.deleted = false")
	Optional<Admin> findByUidAndDeletedIsFalse(@Param("uid") UUID uid);

	@Query("SELECT a FROM Admin a WHERE a.ulid = :ulid AND a.deleted = false")
	Optional<Admin> findByUidAndDeletedIsFalse(@Param("ulid") byte[] ulid);

    boolean existsByEmail(String email);
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByUid(UUID uid);
    Optional<Admin> findByUlid(byte[] uid);

}

