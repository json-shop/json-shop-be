package deepdive.jsonstore.domain.admin.repository;

import deepdive.jsonstore.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUidAndDeletedFalse(UUID uid);

    Optional<Admin> findByUlidAndDeletedFalse(byte[] ulid);

    boolean existsByEmail(String email);

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByUid(UUID uid);

    Optional<Admin> findByUlid(byte[] uid);

}

