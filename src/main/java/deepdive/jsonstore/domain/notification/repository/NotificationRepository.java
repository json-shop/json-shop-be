package deepdive.jsonstore.domain.notification.repository;

import deepdive.jsonstore.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // UUID 기반 조회로 변경
    @EntityGraph(attributePaths = {"member"})
    List<Notification> findByMember_UidOrderByCreatedAtDesc(UUID memberUid);

    @EntityGraph(attributePaths = {"member"})
    List<Notification> findAllByMember_UlidOrderByUlidDesc(byte[] ulid);
}
