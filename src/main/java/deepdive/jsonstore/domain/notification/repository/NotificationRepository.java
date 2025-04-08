package deepdive.jsonstore.domain.notification.repository;

import deepdive.jsonstore.domain.notification.entity.Notification;
import deepdive.jsonstore.domain.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 회원(member)에게 속한 모든 알림 조회
    List<Notification> findByMember(Member member);

    // Notification 조회 시 연관된 member도 한 번에 함께 조회
    @EntityGraph(attributePaths = {"member"})
    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
