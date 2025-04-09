package deepdive.jsonstore.domain.member.service;

import com.google.firebase.messaging.FirebaseMessaging;
import deepdive.jsonstore.common.config.FirebaseConfig;
import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.dto.UpdateMemberRequestDTO;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@Rollback
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
}