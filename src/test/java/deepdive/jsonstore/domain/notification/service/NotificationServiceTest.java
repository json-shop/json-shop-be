package deepdive.jsonstore.domain.notification.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.common.exception.NotificationException;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
            Long memberId = 1L;
            String token = "abc123";

            doNothing().when(validationService).validateMemberExists(memberId);

            assertDoesNotThrow(() -> notificationService.saveToken(memberId, token));
            verify(redisTemplate.opsForValue()).set("fcm:token:" + memberId, token);
        }

        @Test
        @DisplayName("실패 - Redis 저장 중 예외 발생")
        void fail_unexpectedError() {
            Long memberId = 1L;
            String token = "abc123";
            doNothing().when(validationService).validateMemberExists(memberId);
            doThrow(new RuntimeException("Redis 서버에 문제가 발생했습니다")).when(valueOperations).set(anyString(), anyString());

            NotificationException ex = assertThrows(NotificationException.class,
                    () -> notificationService.saveToken(memberId, token));

            assertEquals(JsonStoreErrorCode.REDIS_SERVER_ERROR, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("sendNotification 테스트")
    class SendNotification {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long memberId = 1L;
            String title = "Test Title";
            String body = "Test Body";
            String token = "validToken";
            Member member = Member.builder().id(memberId).build();

            ApiFuture<String> future = mock(ApiFuture.class);
            when(future.get()).thenReturn("messageId");

            when(validationService.validateAndGetFcmToken(memberId)).thenReturn(token);
            when(validationService.validateAndGetMember(memberId)).thenReturn(member);
            when(firebaseMessaging.sendAsync(any(Message.class))).thenReturn(future);

            assertDoesNotThrow(() -> notificationService.sendNotification(memberId, title, body));
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("실패 - Firebase 메시지 전송 중 예외 발생")
        void fail_sendException() throws Exception {
            Long memberId = 1L;
            String title = "Fail Title";
            String body = "Fail Body";
            String token = "token";
            Member member = Member.builder().id(memberId).build();

            ApiFuture<String> future = mock(ApiFuture.class);
            when(future.get()).thenThrow(new ExecutionException(new RuntimeException("Firebase error")));

            when(validationService.validateAndGetFcmToken(memberId)).thenReturn(token);
            when(validationService.validateAndGetMember(memberId)).thenReturn(member);
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(firebaseMessaging.sendAsync(any(Message.class))).thenReturn(future);

            CommonException ex = assertThrows(CommonException.InternalServerException.class,
                    () -> notificationService.sendNotification(memberId, title, body));
            assertEquals(JsonStoreErrorCode.SERVER_ERROR, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("getNotificationHistory 테스트")
    class GetNotificationHistory {

        @Test
        @DisplayName("성공 - 알림 내역이 존재할 경우")
        void success_whenNotificationExists() {
            // given
            Long memberId = 1L;
            List<Notification> expectedNotifications = List.of(
                    new Notification(), new Notification() // 필요 시 필드 채워도 돼
            );
            when(notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId))
                    .thenReturn(expectedNotifications);

            // when
            List<Notification> actualNotifications = notificationService.getNotificationHistory(memberId);

            // then
            assertThat(actualNotifications).isEqualTo(expectedNotifications);
            verify(notificationRepository, times(1)).findByMemberIdOrderByCreatedAtDesc(memberId);
        }

        @Test
        @DisplayName("성공 - 알림 내역이 없는 경우")
        void success_whenNotificationIsEmpty() {
            // given
            Long memberId = 2L;
            when(notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId))
                    .thenReturn(Collections.emptyList());

            // when
            List<Notification> actualNotifications = notificationService.getNotificationHistory(memberId);

            // then
            assertThat(actualNotifications).isEqualTo(Collections.emptyList());
            verify(notificationRepository, times(1)).findByMemberIdOrderByCreatedAtDesc(memberId);
        }
    }
}
