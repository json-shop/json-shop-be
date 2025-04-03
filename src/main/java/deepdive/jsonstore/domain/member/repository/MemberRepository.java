package deepdive.jsonstore.domain.member.repository;


import deepdive.jsonstore.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {


    boolean existsByEmail(String email);

}
