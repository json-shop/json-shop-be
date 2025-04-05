package deepdive.jsonstore.domain.cart.repository;

import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 특정 회원의 장바구니 조회 (회원당 하나만 존재)
    Optional<Cart> findByMember(Member member);
}
