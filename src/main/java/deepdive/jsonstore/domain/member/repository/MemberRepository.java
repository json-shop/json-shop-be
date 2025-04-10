package deepdive.jsonstore.domain.member.repository;


import deepdive.jsonstore.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.UUID;


public interface MemberRepository extends JpaRepository<Member, Long> {


    boolean existsByEmail(String email);

    Optional<Member> findByEmailAndIsDeletedFalse(String email);

    Optional<Member> findByUid(UUID uid);
  
    Optional<Member> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM Member m WHERE m.isDeleted = true AND m.deletedAt <= :cutoffDate")
    void deleteAllSoftDeletedBefore(@Param("cutoffDate") LocalDateTime cutoffDate);


}
