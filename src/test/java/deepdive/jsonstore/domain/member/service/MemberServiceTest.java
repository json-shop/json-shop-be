package deepdive.jsonstore.domain.member.service;

import com.google.firebase.messaging.FirebaseMessaging;
import deepdive.jsonstore.common.config.FirebaseConfig;
import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.common.exception.MemberException;
import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.dto.UpdateMemberRequestDTO;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.exception.MemberErrorCode;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.member.util.MemberUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @Mock
    private FirebaseConfig firebaseConfig;

    @Mock
    private RedisTestService redisTestService;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private MemberUtil memberUtil;

    @Mock
    private MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(
                memberRepository,
                passwordEncoder,
                memberUtil
        );
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void resetPassword_success() {
        // given
        String currentPassword = "pass";  // 현재 비밀번호
        String newPassword = "newpass";   // 새 비밀번호

        // passwordEncoder로 현재 비밀번호 암호화
        String encodedCurrentPassword = passwordEncoder.encode(currentPassword);

        Member member = Member.builder()
                .email("test@example.com")
                .username("testuser")
                .password(encodedCurrentPassword)  // 저장된 암호화된 비밀번호
                .isDeleted(false)
                .build();

        when(memberRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(member)); //findByEmail 호출되면 db에 접근하는게 아니라 위에 만든 member 객체를 넘겨줌

        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO(currentPassword, newPassword, newPassword);

        // when
        memberService.resetPW("test@example.com", dto);

        // then
        assertThat(passwordEncoder.matches(newPassword, member.getPassword())).isTrue();
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

        given(memberRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(member));

        UpdateMemberRequestDTO dto = new UpdateMemberRequestDTO("New Name", "010-1234-5678");

        // when
        memberService.updateMember("test@example.com", dto);

        // then
        assertThat(member.getUsername()).isEqualTo("New Name");
        assertThat(member.getPhone()).isEqualTo("010-1234-5678");
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
