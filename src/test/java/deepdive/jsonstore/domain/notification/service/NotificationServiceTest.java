package deepdive.jsonstore.domain.notification.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.notification.dto.NotificationHistoryResponse;
import deepdive.jsonstore.domain.notification.entity.NotificationCategory;
import deepdive.jsonstore.domain.notification.exception.NotificationException;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.notification.entity.Notification;
import deepdive.jsonstore.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private NotificationValidationService validationService;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("saveToken 테스트")
    class SaveToken {

        @Test
        @DisplayName("성공")
        void success() {
            UUID memberUid = UUID.randomUUID();
            String token = "abc123";

            doNothing().when(validationService).validateMemberExists(memberUid);

            assertDoesNotThrow(() -> notificationService.saveToken(memberUid, token));
            verify(redisTemplate.opsForValue()).set("fcm:token:" + memberUid, token);
        }
    }

    @Nested
    @DisplayName("sendNotification 테스트")
    class SendNotification {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID memberUid = UUID.randomUUID();
            String title = "Test Title";
            String body = "Test Body";
            String token = "validToken";
            Member member = Member.builder().uid(memberUid).build();

            ApiFuture<String> future = mock(ApiFuture.class);
            when(future.get()).thenReturn("messageId");

            when(validationService.validateAndGetFcmToken(memberUid)).thenReturn(token);
            when(validationService.validateAndGetMember(memberUid)).thenReturn(member);
            when(firebaseMessaging.sendAsync(any(Message.class))).thenReturn(future);

            assertDoesNotThrow(() -> notificationService.sendNotification(memberUid, title, body, NotificationCategory.SAVE));
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("실패 - Firebase 메시지 전송 중 예외 발생")
        void fail_sendException() throws Exception {
            UUID memberUid = UUID.randomUUID();
            String title = "Fail Title";
            String body = "Fail Body";
            String token = "token";
            Member member = Member.builder().uid(memberUid).build();

            ApiFuture<String> future = mock(ApiFuture.class);
            when(future.get()).thenThrow(new ExecutionException(new RuntimeException("Firebase error")));

            when(validationService.validateAndGetFcmToken(memberUid)).thenReturn(token);
            when(validationService.validateAndGetMember(memberUid)).thenReturn(member);
            when(memberRepository.findByUid(memberUid)).thenReturn(Optional.of(member));
            when(firebaseMessaging.sendAsync(any(Message.class))).thenReturn(future);

            CommonException ex = assertThrows(CommonException.InternalServerException.class,
                    () -> notificationService.sendNotification(memberUid, title, body, NotificationCategory.SAVE));
            assertEquals(JsonStoreErrorCode.SERVER_ERROR, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("getNotificationHistory 테스트")
    class GetNotificationHistory {

        @Test
        @DisplayName("성공 - 알림 내역이 존재할 경우")
        void success_whenNotificationExists() {
            UUID memberUid = UUID.randomUUID();

            Member mockMember = Member.builder()
                    .uid(memberUid)
                    .build();

            Notification notification1 = Notification.builder()
                    .id(1L)
                    .title("제목1")
                    .body("내용1")
                    .category(NotificationCategory.SAVE)
                    .member(mockMember)
                    .build();

            Notification notification2 = Notification.builder()
                    .id(1L)
                    .title("제목2")
                    .body("내용2")
                    .category(NotificationCategory.SAVE)
                    .member(mockMember)
                    .build();

            List<Notification> expectedNotifications = List.of(notification1, notification2);

            when(notificationRepository.findByMember_UidOrderByCreatedAtDesc(memberUid))
                    .thenReturn(expectedNotifications);

            List<NotificationHistoryResponse> actualNotifications = notificationService.getNotificationHistory(memberUid);

            assertThat(actualNotifications).hasSize(2);
            assertThat(actualNotifications.get(0).getTitle()).isEqualTo("제목1");
            assertThat(actualNotifications.get(1).getTitle()).isEqualTo("제목2");
            verify(notificationRepository, times(1)).findByMember_UidOrderByCreatedAtDesc(memberUid);
        }

        @Test
        @DisplayName("성공 - 알림 내역이 없는 경우")
        void success_whenNotificationIsEmpty() {
            UUID memberUid = UUID.randomUUID();
            when(notificationRepository.findByMember_UidOrderByCreatedAtDesc(memberUid))
                    .thenReturn(Collections.emptyList());

            List<NotificationHistoryResponse> actualNotifications = notificationService.getNotificationHistory(memberUid);

            assertThat(actualNotifications).isEqualTo(Collections.emptyList());
            verify(notificationRepository, times(1)).findByMember_UidOrderByCreatedAtDesc(memberUid);
        }
    }
}
