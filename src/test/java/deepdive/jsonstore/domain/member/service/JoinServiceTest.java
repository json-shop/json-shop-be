package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.JoinException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.dto.JoinRequest;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(JoinServiceTest.class);

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private JoinValidationService joinValidationService;  // JoinValidationService 모킹 추가

    @InjectMocks
    private JoinService joinService;

    private JoinRequest joinRequest;

    @BeforeEach
    void setUp() {
        joinRequest = new JoinRequest(
                "test@example.com",
                "password123",
                "password123",
                "TestUser",
                "01012345678"
        );
    }

    @Nested
    @DisplayName("joinProcess 테스트")
    class JoinProcessTest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            doNothing().when(joinValidationService).validateJoinRequest(joinRequest);  // ValidationService 동작 추가
            when(bCryptPasswordEncoder.encode(joinRequest.password())).thenReturn("encryptedPassword");
            when(memberRepository.existsByEmail(joinRequest.email())).thenReturn(false);

            // when
            joinService.joinProcess(joinRequest);

            // then
            verify(joinValidationService, times(1)).validateJoinRequest(joinRequest);  // validateJoinRequest 검증 추가
            verify(bCryptPasswordEncoder, times(1)).encode(joinRequest.password());
            verify(memberRepository, times(1)).save(any(Member.class));

            logger.info("회원 가입 성공: {}", joinRequest.email());
        }

        @Test
        @DisplayName("실패 - 이메일 중복")
        void fail_이메일_중복_예외() {
            // given
            when(memberRepository.existsByEmail(joinRequest.email())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> joinService.joinProcess(joinRequest))
                    .isInstanceOf(JoinException.DuplicateEmailException.class)
                    .hasMessage(JsonStoreErrorCode.DUPLICATE_EMAIL.getMessage())
                    .satisfies(e -> logger.error("예외 발생: {}", e.getMessage()));

            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void fail_비밀번호_불일치_예외() {
            // given
            JoinRequest invalidJoinRequest = new JoinRequest(
                    joinRequest.email(),
                    joinRequest.password(),
                    "differentPassword", // 잘못된 비밀번호 확인 값
                    joinRequest.username(),
                    joinRequest.phone()
            );

            // when & then
            assertThatThrownBy(() -> joinService.joinProcess(invalidJoinRequest))
                    .isInstanceOf(JoinException.PasswordMismatchException.class)
                    .hasMessage(JsonStoreErrorCode.PASSWORD_MISMATCH.getMessage())
                    .satisfies(e -> logger.error("예외 발생: {}", e.getMessage()));

            verify(memberRepository, never()).save(any(Member.class));
        }
    }
}