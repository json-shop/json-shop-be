package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.CustomException;
import deepdive.jsonstore.domain.member.dto.JoinResponse;
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

    @InjectMocks
    private JoinService joinService;

    private JoinResponse joinResponse;

    @BeforeEach
    void setUp() {
        joinResponse = new JoinResponse(
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
            when(bCryptPasswordEncoder.encode(joinResponse.password())).thenReturn("encryptedPassword");
            when(memberRepository.existsByEmail(joinResponse.email())).thenReturn(false);

            // when
            joinService.joinProcess(joinResponse);

            // then
            verify(bCryptPasswordEncoder, times(1)).encode(joinResponse.password());
            verify(memberRepository, times(1)).save(any(Member.class));

            logger.info("회원 가입 성공: {}", joinResponse.email());
        }

        @Test
        @DisplayName("실패 - 이메일 중복")
        void fail_이메일_중복_예외() {
            // given
            when(memberRepository.existsByEmail(joinResponse.email())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> joinService.joinProcess(joinResponse))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 존재하는 이메일입니다.")
                    .satisfies(e -> logger.error("예외 발생: {}", e.getMessage()));

            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void fail_비밀번호_불일치_예외() {
            // given
            JoinResponse invalidJoinResponse = new JoinResponse(
                    joinResponse.email(),
                    joinResponse.password(),
                    "differentPassword", // 잘못된 비밀번호 확인 값
                    joinResponse.username(),
                    joinResponse.phone()
            );

            // when & then
            assertThatThrownBy(() -> joinService.joinProcess(invalidJoinResponse))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("비밀번호가 일치하지 않습니다.")
                    .satisfies(e -> logger.error("예외 발생: {}", e.getMessage()));

            verify(memberRepository, never()).save(any(Member.class));
        }
    }
}
