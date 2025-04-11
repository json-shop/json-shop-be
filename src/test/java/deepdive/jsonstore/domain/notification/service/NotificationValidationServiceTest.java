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
            Long memberId = 1L;
            String token = "abc123";

            when(valueOperations.get("fcm:token:" + memberId)).thenReturn(token);

            String result = validationService.validateAndGetFcmToken(memberId);
            assertEquals(token, result);
        }

        @Test
        @DisplayName("실패 - 토큰이 존재하지 않음")
        void fail_tokenMissing() {
            Long memberId = 1L;

            when(valueOperations.get("fcm:token:" + memberId)).thenReturn(null);

            NotificationException ex = assertThrows(NotificationException.class,
                    () -> validationService.validateAndGetFcmToken(memberId));
            assertEquals(JsonStoreErrorCode.MISSING_FCM_TOKEN, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("validateMemberExists 테스트")
    class ValidateMemberExists {

        @Test
        @DisplayName("성공")
        void success() {
            Long memberId = 1L;
            when(memberRepository.existsById(memberId)).thenReturn(true);

            assertDoesNotThrow(() -> validationService.validateMemberExists(memberId));
        }

        @Test
        @DisplayName("실패 - 회원이 존재하지 않음")
        void fail_notFound() {
            Long memberId = 1L;
            when(memberRepository.existsById(memberId)).thenReturn(false);

            NotificationException ex = assertThrows(NotificationException.class,
                    () -> validationService.validateMemberExists(memberId));
            assertEquals(JsonStoreErrorCode.NOTIFICATION_MEMBER_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("validateAndGetMember 테스트")
    class ValidateAndGetMember {

        @Test
        @DisplayName("성공")
        void success() {
            Long memberId = 1L;
            Member member = Member.builder().id(memberId).build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

            Member result = validationService.validateAndGetMember(memberId);
            assertEquals(member, result);
        }

        @Test
        @DisplayName("실패 - 회원이 존재하지 않음")
        void fail_notFound() {
            Long memberId = 1L;

            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            NotificationException ex = assertThrows(NotificationException.class,
                    () -> validationService.validateAndGetMember(memberId));
            assertEquals(JsonStoreErrorCode.NOTIFICATION_MEMBER_NOT_FOUND, ex.getErrorCode());
        }
    }
}
