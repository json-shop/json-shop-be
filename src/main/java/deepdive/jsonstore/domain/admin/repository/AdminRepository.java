package deepdive.jsonstore.domain.admin.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import deepdive.jsonstore.domain.admin.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

}

