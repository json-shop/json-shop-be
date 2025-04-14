package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.auth.service.CustomMemberDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberJwtTokenProviderTest {

    @InjectMocks
    private MemberJwtTokenProvider memberJwtTokenProvider;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private CustomMemberDetailsService customMemberDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Key mockKey;

    private UUID testUuid;
    private final String SECRET_KEY = "testSecretKeyWithAtLeast256BitsForHMACSHA";

    @BeforeEach
    void setUp() {
        // UUID 생성
        testUuid = UUID.randomUUID();

        // JwtTokenProvider 프로퍼티 설정
        ReflectionTestUtils.setField(memberJwtTokenProvider, "secretKey", SECRET_KEY);

        // init 메서드 호출을 위한 기본 설정만 유지
        when(jwtTokenUtil.getSigningKey(anyString())).thenReturn(mockKey);

        // init 메서드 호출
        memberJwtTokenProvider.init();
    }

    @Test
    @DisplayName("토큰 생성 테스트")
    void generateTokenTest() {
        // given
        CustomMemberDetails mockMemberDetails = mock(CustomMemberDetails.class);
        when(mockMemberDetails.getUid()).thenReturn(testUuid);

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(mockMemberDetails).getAuthorities();

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockMemberDetails);

        JwtTokenDto expectedToken = new JwtTokenDto("Bearer", "accessToken");
        when(jwtTokenUtil.generateToken(eq(testUuid), eq("ROLE_USER"), any(Key.class)))
                .thenReturn(expectedToken);

        // when
        JwtTokenDto result = memberJwtTokenProvider.generateToken(mockAuthentication);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGrantType()).isEqualTo("Bearer");
        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        verify(jwtTokenUtil).generateToken(eq(testUuid), eq("ROLE_USER"), any(Key.class));
    }

    @Test
    @DisplayName("토큰 유효성 검사 테스트")
    void validateTokenTest() {
        // given
        String token = "validToken";
        when(jwtTokenUtil.validateToken(eq(token), any(Key.class))).thenReturn(true);

        // when
        boolean result = memberJwtTokenProvider.validateToken(token);

        // then
        assertThat(result).isTrue();
        verify(jwtTokenUtil).validateToken(eq(token), any(Key.class));
    }

    @Test
    @DisplayName("Claims 파싱 테스트")
    void parseClaimsTest() {
        // given
        String token = "validToken";
        Claims mockClaims = mock(Claims.class);
        when(jwtTokenUtil.parseClaims(eq(token), any(Key.class))).thenReturn(mockClaims);

        // when
        Claims result = memberJwtTokenProvider.parseClaims(token);

        // then
        assertThat(result).isEqualTo(mockClaims);
        verify(jwtTokenUtil).parseClaims(eq(token), any(Key.class));
    }

    @Test
    @DisplayName("요청 헤더에서 토큰 추출 테스트 - 유효한 Bearer 토큰")
    void resolveTokenWithValidBearerTokenTest() {
        // given
        String bearerToken = "Bearer validToken";
        when(request.getHeader("Authorization")).thenReturn(bearerToken);

        // when
        String result = memberJwtTokenProvider.resolveToken(request);

        // then
        assertThat(result).isEqualTo("validToken");
    }

    @Test
    @DisplayName("요청 헤더에서 토큰 추출 테스트 - Bearer 접두사 없는 경우")
    void resolveTokenWithNoBearerPrefixTest() {
        // given
        when(request.getHeader("Authorization")).thenReturn("validToken");

        // when
        String result = memberJwtTokenProvider.resolveToken(request);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("요청 헤더에서 토큰 추출 테스트 - Authorization 헤더 없는 경우")
    void resolveTokenWithNoAuthorizationHeaderTest() {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        String result = memberJwtTokenProvider.resolveToken(request);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("토큰 기반 인증 객체 생성 및 UUID 추출 테스트")
    void getAuthenticationAndExtractUuidTest() {
        // given
        String token = "validToken";
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn(testUuid.toString());
        when(jwtTokenUtil.parseClaims(eq(token), any(Key.class))).thenReturn(mockClaims);

        CustomMemberDetails mockMemberDetails = mock(CustomMemberDetails.class);
        when(customMemberDetailsService.loadUserByUuid(testUuid)).thenReturn(mockMemberDetails);

        // when
        Authentication result = memberJwtTokenProvider.getAuthentication(token);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isEqualTo(mockMemberDetails);
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);

        // UUID 추출 검증
        verify(mockClaims).getSubject();
        verify(customMemberDetailsService).loadUserByUuid(testUuid);
    }
}