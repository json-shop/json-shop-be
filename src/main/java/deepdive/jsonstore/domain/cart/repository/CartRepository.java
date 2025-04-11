package deepdive.jsonstore.domain.cart.repository;

import deepdive.jsonstore.domain.cart.entity.Cart;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.product.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // 멤버와 상품을 기반으로 카트 목록 조회
    Cart findByMemberAndProduct(Member member, Product product);

    // 멤버ID를 기반으로 카트 목록 조회
    @EntityGraph(attributePaths = {"product", "member"})
    List<Cart> findByMemberId(Long memberId);

}
