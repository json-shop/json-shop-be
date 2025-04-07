package deepdive.jsonstore.domain.member.repository;


import deepdive.jsonstore.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, Long> {


    boolean existsByEmail(String email);

    Optional<Member> findByUid(UUID uid);

    Optional<Member> findByEmail(String email);
}
