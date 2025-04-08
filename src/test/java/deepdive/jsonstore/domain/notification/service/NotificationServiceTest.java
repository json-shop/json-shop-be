package deepdive.jsonstore.domain.notification.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

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
        @DisplayName("성공 케이스")
        void success() {
            // given
            Long memberId = 1L;
            String token = "abc123";

            when(memberRepository.existsById(memberId)).thenReturn(true);

            // when & then
            assertDoesNotThrow(() -> notificationService.saveToken(memberId, token));
            verify(redisTemplate.opsForValue()).set("fcm:token:" + memberId, token);
        }

        @Test
        @DisplayName("memberId가 null일 때 예외")
        void fail_memberIdNull() {
            // given
            String token = "abc123";

            // when & then
            CommonException ex = assertThrows(CommonException.class,
                    () -> notificationService.saveToken(null, token));
            assertEquals(JsonStoreErrorCode.UNAUTHORIZED, ex.getErrorCode());
        }

        @Test
        @DisplayName("token이 null일 때 예외")
        void fail_tokenNull() {
            Long memberId = 1L;
            String token = null;

            // when & then
            CommonException ex = assertThrows(CommonException.InvalidInputException.class,
                    () -> notificationService.saveToken(memberId, token));
            assertEquals(JsonStoreErrorCode.INVALID_INPUT_PARAMETER, ex.getErrorCode());
        }

        @Test
        @DisplayName("token이 빈 문자열일 때 예외")
        void fail_tokenEmpty() {
            Long memberId = 1L;
            String token = "   ";

            CommonException ex = assertThrows(CommonException.InvalidInputException.class,
                    () -> notificationService.saveToken(memberId, token));
            assertEquals(JsonStoreErrorCode.INVALID_INPUT_PARAMETER, ex.getErrorCode());
        }

        @Test
        @DisplayName("존재하지 않는 memberId일 때 예외")
        void fail_memberNotFound() {
            Long memberId = 999L;
            String token = "abc123";

            when(memberRepository.existsById(memberId)).thenReturn(false);

            CommonException ex = assertThrows(CommonException.class,
                    () -> notificationService.saveToken(memberId, token));
            assertEquals(JsonStoreErrorCode.UNAUTHORIZED, ex.getErrorCode());
        }

        @Test
        @DisplayName("Redis 저장 중 예기치 않은 예외 발생 시 500 예외 반환")
        void fail_unexpectedError() {
            Long memberId = 1L;
            String token = "abc123";

            when(memberRepository.existsById(memberId)).thenReturn(true);
            doThrow(new RuntimeException("Redis error")).when(valueOperations).set(anyString(), anyString());

            CommonException ex = assertThrows(CommonException.InternalServerException.class,
                    () -> notificationService.saveToken(memberId, token));
            assertEquals(JsonStoreErrorCode.SERVER_ERROR, ex.getErrorCode());
        }
    }
}
