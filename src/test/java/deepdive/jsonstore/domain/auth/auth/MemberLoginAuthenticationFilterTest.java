package deepdive.jsonstore.domain.auth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberLoginAuthenticationFilterTest {

    private MemberLoginAuthenticationFilter filter;
    private AuthenticationManager authenticationManager;
    private MemberJwtTokenProvider memberJwtTokenProvider;
    private ObjectMapper objectMapper;

    @BeforeEach
    @DisplayName("테스트를 위한 객체 초기화")
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        memberJwtTokenProvider = mock(MemberJwtTokenProvider.class);
        objectMapper = new ObjectMapper();
        filter = new MemberLoginAuthenticationFilter(authenticationManager, memberJwtTokenProvider);
    }

    @Test
    @DisplayName("회원 로그인 시도 테스트")
    void testAttemptAuthentication() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        LoginRequest loginRequest = new LoginRequest("member@example.com", "password");
        request.setContent(objectMapper.writeValueAsBytes(loginRequest));

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken("member@example.com", "password");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(authRequest)).thenReturn(authentication);

        Authentication result = filter.attemptAuthentication(request, new MockHttpServletResponse());

        assertEquals(authentication, result);
        verify(authenticationManager).authenticate(authRequest);
    }

    @Test
    @DisplayName("회원 로그인 실패 테스트")
    void testAttemptAuthenticationWithWrongPassword() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        LoginRequest loginRequest = new LoginRequest("member@example.com", "wrongpassword");
        request.setContent(objectMapper.writeValueAsBytes(loginRequest));

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken("member@example.com", "wrongpassword");

        when(authenticationManager.authenticate(authRequest))
                .thenThrow(new AuthenticationException("Bad credentials") {});

        assertThrows(AuthenticationException.class, () -> {
            filter.attemptAuthentication(request, new MockHttpServletResponse());
        });

        verify(authenticationManager).authenticate(authRequest);
    }

    @Test
    @DisplayName("회원 인증 성공 테스트")
    void testSuccessfulAuthentication() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        Authentication authentication = mock(Authentication.class);

        JwtTokenDto tokenDto = new JwtTokenDto("Bearer", "token");
        when(memberJwtTokenProvider.generateToken(authentication)).thenReturn(tokenDto);

        filter.successfulAuthentication(request, response, chain, authentication);

        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"grantType\":\"Bearer\",\"accessToken\":\"token\"}", response.getContentAsString());
    }

    @Test
    @DisplayName("회원 인증 실패 테스트")
    void testUnsuccessfulAuthentication() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = mock(AuthenticationException.class);

        filter.unsuccessfulAuthentication(request, response, exception);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"error\": \"Login failed\"}", response.getContentAsString());
    }
}
