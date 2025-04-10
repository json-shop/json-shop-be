package deepdive.jsonstore.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.notification.dto.NotificationHistoryResponse;
import deepdive.jsonstore.domain.notification.entity.Notification;
import deepdive.jsonstore.domain.notification.entity.NotificationCategory;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final NotificationValidationService validationService;
    private final FirebaseMessaging firebaseMessaging;

    public void saveToken(Long memberId, String token) {
        // 멤버 검증
        validationService.validateMemberExists(memberId);

        // redis에 토큰 저장
        redisTemplate.opsForValue().set("fcm:token:" + memberId, token);
    }

    // 알림 전송
    public void sendNotification(Long memberId, String title, String body) {
        try {
            String token = validationService.validateAndGetFcmToken(memberId);
            Member member = validationService.validateAndGetMember(memberId);

            Message fcmMessage = Message.builder()
                    .setToken(token)
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(WebpushNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                            .build())
                    .build();

            String response = firebaseMessaging.sendAsync(fcmMessage).get();
            log.info("FCM message sent successfully to user {} with message ID: {}", memberId, response);

            saveNotificationRecord(member, title, body, NotificationCategory.SAVE);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error sending FCM message to user {}: {}", memberId, e.getMessage());

            memberRepository.findById(memberId).ifPresent(member ->
                    saveNotificationRecord(member, title, body, NotificationCategory.ERROR));

            throw new CommonException.InternalServerException();
        }
    }

    // 메시지 전송 기록 저장
    private void saveNotificationRecord(Member member, String title, String body, NotificationCategory category) {
        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .category(category)
                .member(member)
                .build();

        notificationRepository.save(notification);
    }

    // 사용자별 알림 내역 조회
    public List<NotificationHistoryResponse> getNotificationHistory(Long memberId) {
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(notification -> new NotificationHistoryResponse(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getBody(),
                        notification.getCategory(),
                        notification.getMember().getId(),  // 여기서 ID만 추출
                        notification.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
