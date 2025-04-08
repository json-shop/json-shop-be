package deepdive.jsonstore.domain.admin.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import deepdive.jsonstore.domain.admin.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

	Optional<Admin> findByUidAndDeletedIsFalse(UUID uid);

}

