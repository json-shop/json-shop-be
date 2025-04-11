package deepdive.jsonstore.domain.notification.service;

import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.notification.exception.NotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationValidationServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private NotificationValidationService validationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("validateAndGetFcmToken 테스트")
    class ValidateAndGetFcmToken {

        @Test
        @DisplayName("성공")
        void success() {
            UUID memberUid = UUID.randomUUID();
            String token = "abc123";

            when(valueOperations.get("fcm:token:" + memberUid)).thenReturn(token);

            String result = validationService.validateAndGetFcmToken(memberUid);
            assertEquals(token, result);
        }

        @Test
        @DisplayName("실패 - 토큰이 존재하지 않음")
        void fail_tokenMissing() {
            UUID memberUid = UUID.randomUUID();

            when(valueOperations.get("fcm:token:" + memberUid)).thenReturn(null);

            NotificationException ex = assertThrows(NotificationException.class,
                    () -> validationService.validateAndGetFcmToken(memberUid));
            assertEquals(JsonStoreErrorCode.MISSING_FCM_TOKEN, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("validateMemberExists 테스트")
    class ValidateMemberExists {

        @Test
        @DisplayName("성공")
        void success() {
            UUID memberUid = UUID.randomUUID();
            when(memberRepository.existsByUid(memberUid)).thenReturn(true);

            assertDoesNotThrow(() -> validationService.validateMemberExists(memberUid));
        }

        @Test
        @DisplayName("실패 - 회원이 존재하지 않음")
        void fail_notFound() {
            UUID memberUid = UUID.randomUUID();
            when(memberRepository.existsByUid(memberUid)).thenReturn(false);

            NotificationException ex = assertThrows(NotificationException.class,
                    () -> validationService.validateMemberExists(memberUid));
            assertEquals(JsonStoreErrorCode.NOTIFICATION_MEMBER_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("validateAndGetMember 테스트")
    class ValidateAndGetMember {

        @Test
        @DisplayName("성공")
        void success() {
            UUID memberUid = UUID.randomUUID();
            Member member = Member.builder().uid(memberUid).build();

            when(memberRepository.findByUid(memberUid)).thenReturn(Optional.of(member));

            Member result = validationService.validateAndGetMember(memberUid);
            assertEquals(member, result);
        }

        @Test
        @DisplayName("실패 - 회원이 존재하지 않음")
        void fail_notFound() {
            UUID memberUid = UUID.randomUUID();

            when(memberRepository.findByUid(memberUid)).thenReturn(Optional.empty());

            NotificationException ex = assertThrows(NotificationException.class,
                    () -> validationService.validateAndGetMember(memberUid));
            assertEquals(JsonStoreErrorCode.NOTIFICATION_MEMBER_NOT_FOUND, ex.getErrorCode());
        }
    }
}
