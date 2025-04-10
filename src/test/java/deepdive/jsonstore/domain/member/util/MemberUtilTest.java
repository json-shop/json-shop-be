package deepdive.jsonstore.domain.member.util;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.member.entity.Member;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemberUtil 단위 테스트")
public class MemberUtilTest {

    private MemberUtil memberUtil;

    @BeforeEach
    void setUp() {
        memberUtil = new MemberUtil();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // 컨텍스트 초기화
    }

    @Test
    @DisplayName("정상적으로 인증된 사용자는 Member 객체를 반환")
    void getCurrentMember_성공_테스트() {
        // given
        Member mockMember = Member.builder()
                .email("test@example.com")
                .username("tester")
                .build();

        CustomMemberDetails customDetails = new CustomMemberDetails(
                mockMember,
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
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 ForbiddenAccessException이 발생")
    void getCurrentMember_실패_테스트() {
        // given: 비정상 사용자
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("other", null)
        );

        // when & then
        assertThrows(AuthException.ForbiddenAccessException.class, () -> {
            memberUtil.getCurrentMember();
        });
    }
}
