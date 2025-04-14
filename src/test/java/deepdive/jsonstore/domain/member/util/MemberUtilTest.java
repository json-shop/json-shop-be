package deepdive.jsonstore.domain.member.util;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemberUtil 단위 테스트")
public class MemberUtilTest {

    private MemberUtil memberUtil;
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        // Mockito를 사용하여 MemberRepository 모킹
        memberRepository = mock(MemberRepository.class);
        memberUtil = new MemberUtil(memberRepository); // MemberUtil에 repository 주입
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // 컨텍스트 초기화
    }

    @Test
    @DisplayName("정상적으로 인증된 사용자는 Member 객체를 반환")
    void getCurrentMember_성공_테스트() {
        // given
        UUID mockUuid = UUID.randomUUID();
        Member mockMember = Member.builder()
                .email("test@example.com")
                .username("tester")
                .uid(mockUuid)
                .build();

        // MemberRepository 모킹
        when(memberRepository.findByUid(mockUuid)).thenReturn(java.util.Optional.of(mockMember));

        CustomMemberDetails customDetails = new CustomMemberDetails(
                mockUuid, // UUID 추가
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customDetails, null, customDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        Member result = memberUtil.getCurrentMember();

        // then
        assertEquals("test@example.com", result.getEmail());
        verify(memberRepository, times(1)).findByUid(mockUuid); // repository 조회 검증
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 UnauthenticatedAccessException이 발생")
    void getCurrentMember_실패_테스트() {
        // given: 비정상 사용자
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("other", null)
        );

        // when & then
        assertThrows(AuthException.UnauthenticatedAccessException.class, () -> {
            memberUtil.getCurrentMember();
        });
    }
}
