package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.MemberException;
import com.google.firebase.messaging.FirebaseMessaging;
import deepdive.jsonstore.common.config.FirebaseConfig;
import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.dto.UpdateMemberRequestDTO;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.exception.MemberErrorCode;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.member.util.MemberUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
@Rollback
@Import(MemberServiceTest.TestConfig.class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @MockitoBean
    private FirebaseConfig firebaseConfig;

    @MockitoBean
    private RedisTestService redisTestService;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberUtil memberUtil;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MemberUtil memberUtil() {
            return Mockito.mock(MemberUtil.class);
        }
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("비밀번호 변경 성공")
    void resetPassword_success() {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .username("testuser")
                .password(passwordEncoder.encode("pass"))
                .isDeleted(false)
                .build();

        memberRepository.save(member);

        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO("pass","newpass", "newpass");

        // when
        memberService.resetPW("test@example.com", dto);

        // then
        Member updated = memberRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("newpass", updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateMember_success() {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .username("Old Name")
                .phone("010-0000-0000")
                .password(passwordEncoder.encode("password"))
                .isDeleted(false)
                .build();

        memberRepository.save(member);

        UpdateMemberRequestDTO dto = new UpdateMemberRequestDTO("New Name", "010-1234-5678");

        // when
        memberService.updateMember("test@example.com", dto);

        // then
        Member updated = memberRepository.findByEmail("test@example.com").orElseThrow();

        assertThat(updated.getUsername()).isEqualTo("New Name");
        assertThat(updated.getPhone()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("성공 - 현재 로그인한 사용자를 소프트 삭제")
    void success_소프트삭제_정상처리() {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .username("testuser")
                .password("encoded-password")
                .isDeleted(false)
                .build();

        memberRepository.save(member);

        given(memberUtil.getCurrentMember()).willReturn(member);

        // when
        memberService.deleteCurrentMember();

        // then
        assertThat(member.getIsDeleted()).isTrue();
        assertThat(member.getDeletedAt()).isNotNull();
        assertThat(member.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("실패 - 이미 삭제된 사용자 예외")
    void fail_이미삭제된사용자_예외() {
        // given
        Member member = Member.builder()
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .isDeleted(true)
                .deletedAt(LocalDateTime.now())
                .build();

        memberRepository.save(member);

        given(memberUtil.getCurrentMember()).willReturn(member);

        // when
        Throwable thrown = catchThrowable(() -> memberService.deleteCurrentMember());

        // then
        assertThat(thrown).isInstanceOf(MemberException.AlreadyDeletedException.class);
        assertThat(((MemberException) thrown).getErrorCode()).isEqualTo(MemberErrorCode.ALREADY_DELETED);
    }

}