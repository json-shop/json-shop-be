package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.MemberException;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.exception.MemberErrorCode;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.member.util.MemberUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberUtil memberUtil;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        when(memberUtil.getCurrentMember()).thenReturn(member);

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

        given(memberUtil.getCurrentMember()).willReturn(member);

        // when
        Throwable thrown = catchThrowable(() -> memberService.deleteCurrentMember());

        // then
        assertThat(thrown).isInstanceOf(MemberException.AlreadyDeletedException.class);
        assertThat(((MemberException) thrown).getErrorCode()).isEqualTo(MemberErrorCode.ALREADY_DELETED);
    }
}
