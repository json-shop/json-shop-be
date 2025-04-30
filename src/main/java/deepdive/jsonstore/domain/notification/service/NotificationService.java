package deepdive.jsonstore.domain.notification.service;

import com.google.firebase.messaging.*;
import deepdive.jsonstore.common.exception.FcmException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.notification.dto.NotificationHistoryResponse;
import deepdive.jsonstore.domain.notification.entity.Notification;
import deepdive.jsonstore.domain.notification.entity.NotificationCategory;
import deepdive.jsonstore.domain.notification.repository.NotificationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
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
    private final MeterRegistry meterRegistry;

    public void saveToken(UUID memberUid, String token) {
        // 멤버 검증
        validationService.validateMemberExists(memberUid);

        // Redis에 토큰 저장
        redisTemplate.opsForValue().set("fcm:token:" + memberUid, token);
    }

    public void sendNotification(UUID memberUid, String title, String body, NotificationCategory category) {
        Member member = validationService.validateAndGetMember(memberUid);
        String token = validationService.validateAndGetFcmToken(memberUid);

        if (token == null || token.isBlank()) {
            handleFailure(member, title, body, "FCM token is missing");
            throw new FcmException.MissingFcmTokenException();
        }

        Message fcmMessage = buildFcmMessage(token, title, body);

        try {
            String response = firebaseMessaging.sendAsync(fcmMessage).get();
            log.info("✅ FCM message sent to user {} with message ID: {}", memberUid, response);
            meterRegistry.counter("business.notification.success").increment();
            saveNotificationRecord(member, title, body, category);

        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FirebaseMessagingException fme) {
                log.error("❌ FCM error for user {} (code={}): {}", memberUid, fme.getErrorCode(), fme.getMessage());
            } else {
                log.error("❌ Unexpected error sending FCM to user {}: {}", memberUid, e.getMessage());
            }

            handleFailure(member, title, body, "FCM send failed");
            throw new CommonException.InternalServerException();
        }
    }

    private Message buildFcmMessage(String token, String title, String body) {
        return Message.builder()
                .setToken(token)
                .setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build())
                .build();
    }

    private void handleFailure(Member member, String title, String body, String logMessage) {
        log.warn(logMessage);
        meterRegistry.counter("business.notification.failure").increment();
        saveNotificationRecord(member, title, body, NotificationCategory.ERROR);
    }

    private void saveNotificationRecord(Member member, String title, String body, NotificationCategory category) {
        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .category(category)
                .member(member)
                .build();
        // ✅ ULID 자동 생성
        notification.generateUlid();

        notificationRepository.save(notification);
    }

    public List<NotificationHistoryResponse> getNotificationHistory(UUID memberUid) {
        return notificationRepository.findByMember_UidOrderByCreatedAtDesc(memberUid).stream()
                .map(notification -> new NotificationHistoryResponse(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getBody(),
                        notification.getCategory(),
                        notification.getMember().getUid(),
                        notification.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<NotificationHistoryResponse> getNotificationHistoryV2(byte[] ulid) {
        return notificationRepository.findAllByMember_UlidOrderByUlidDesc(ulid).stream()
                .map(notification -> new NotificationHistoryResponse(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getBody(),
                        notification.getCategory(),
                        notification.getMember().getUid(),
                        notification.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }


}
