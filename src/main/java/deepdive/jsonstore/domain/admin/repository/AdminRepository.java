package deepdive.jsonstore.domain.admin.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import deepdive.jsonstore.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface AdminRepository extends JpaRepository<Admin, Long> {

	Optional<Admin> findByUidAndDeletedIsFalse(UUID uid);

    boolean existsByEmail(String email);
    Optional<Admin> findByEmail(String email);
}

