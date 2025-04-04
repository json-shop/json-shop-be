package deepdive.jsonstore.domain.notification.repository;

import deepdive.jsonstore.domain.notification.model.Notification;
import deepdive.jsonstore.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 회원(member)에게 속한 모든 알림 조회
    List<Notification> findByMember(Member member);
}
