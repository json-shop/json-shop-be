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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(JoinServiceTest.class);

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private JoinValidationService joinValidationService;

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
            when(bCryptPasswordEncoder.encode(joinRequest.password())).thenReturn("encryptedPassword");

            // when
            joinService.joinProcess(joinRequest);

            // then  입력값 검증 호출 , 비밀번호 암호화, 암호화된 비밀번호를 포함해서 멤버객체가 저장된지 검증
            verify(joinValidationService).validateJoinRequest(joinRequest);
            verify(bCryptPasswordEncoder).encode(joinRequest.password());
            verify(memberRepository).save(any(Member.class));

            logger.info("회원 가입 성공: {}", joinRequest.email());
        }

        @Test
        @DisplayName("실패 - 이메일 중복")
        void fail_이메일_중복_예외() {
            // given
            doThrow(new JoinException.DuplicateEmailException(JsonStoreErrorCode.DUPLICATE_EMAIL))
                    .when(joinValidationService).validateJoinRequest(joinRequest);

            // when
            Throwable thrown = catchThrowable(() -> joinService.joinProcess(joinRequest));

            // then 중복이메일 예외 발생, 에러코드 동일, 예외가 발생했으므로 DB에 저장하는 호출은 호출되면 안된다는 검증
            assertThat(thrown).isInstanceOf(JoinException.DuplicateEmailException.class);
            assertThat(((JoinException) thrown).getErrorCode())
                    .isEqualTo(JsonStoreErrorCode.DUPLICATE_EMAIL);

            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void fail_비밀번호_불일치_예외() {
            // given
            JoinRequest invalidJoinResponse = new JoinRequest(
                    joinRequest.email(),
                    joinRequest.password(),
                    "differentPassword",
                    joinRequest.username(),
                    joinRequest.phone()
            );

            doThrow(new JoinException.PasswordMismatchException(JsonStoreErrorCode.PASSWORD_MISMATCH))
                    .when(joinValidationService).validateJoinRequest(invalidJoinResponse);

            // when
            Throwable thrown = catchThrowable(() -> joinService.joinProcess(invalidJoinResponse));

            // then  비밀번호 불일치 예외 발생, 에러코드 동일, 예외가 발생했으므로 DB에 저장하는 호출은 호출되면 안된다는 검증
            assertThat(thrown).isInstanceOf(JoinException.PasswordMismatchException.class);
            assertThat(((JoinException) thrown).getErrorCode())
                    .isEqualTo(JsonStoreErrorCode.PASSWORD_MISMATCH);

            verify(memberRepository, never()).save(any(Member.class));
                    }
    }
}
