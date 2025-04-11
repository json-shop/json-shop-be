package deepdive.jsonstore.domain.auth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginRequest;
import deepdive.jsonstore.common.exception.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

class AdminLoginAuthenticationFilterTest {

    private AdminLoginAuthenticationFilter filter;
    private AuthenticationManager authenticationManager;
    private AdminJwtTokenProvider adminJwtTokenProvider;
    private ObjectMapper objectMapper;

    @BeforeEach
    @DisplayName("테스트를 위한 객체 초기화")
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        adminJwtTokenProvider = mock(AdminJwtTokenProvider.class);
        objectMapper = new ObjectMapper();
        filter = new AdminLoginAuthenticationFilter(authenticationManager, adminJwtTokenProvider);
    }

    @Test
    @DisplayName("로그인 시도 테스트")
    void testAttemptAuthentication() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        LoginRequest loginRequest = new LoginRequest("admin@example.com", "password");
        request.setContent(objectMapper.writeValueAsBytes(loginRequest));

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken("admin@example.com", "password");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(authRequest)).thenReturn(authentication);

        Authentication result = filter.attemptAuthentication(request, new MockHttpServletResponse());

        assertEquals(authentication, result);
        verify(authenticationManager).authenticate(authRequest);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 시 예외 발생 테스트")
    void testAttemptAuthenticationWithWrongPassword() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        LoginRequest loginRequest = new LoginRequest("admin@example.com", "wrongpassword");
        request.setContent(objectMapper.writeValueAsBytes(loginRequest));

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken("admin@example.com", "wrongpassword");

        when(authenticationManager.authenticate(authRequest))
                .thenThrow(new AuthException.AdminLoginFailedException());

        assertThrows(AuthException.AdminLoginFailedException.class, () -> {
            filter.attemptAuthentication(request, new MockHttpServletResponse());
        });

        verify(authenticationManager).authenticate(authRequest);
    }

    @Test
    @DisplayName("인증 성공 시 JWT 응답 테스트")
    void testSuccessfulAuthentication() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        Authentication authentication = mock(Authentication.class);

        JwtTokenDto tokenDto = new JwtTokenDto("Bearer", "token");
        when(adminJwtTokenProvider.generateToken(authentication)).thenReturn(tokenDto);

        filter.successfulAuthentication(request, response, chain, authentication);

        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"grantType\":\"Bearer\",\"accessToken\":\"token\"}", response.getContentAsString());
    }

    @Test
    @DisplayName("인증 실패 시 AuthException 발생 테스트")
    void testUnsuccessfulAuthentication_throwsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = mock(AuthenticationException.class);

        assertThrows(AuthException.AdminLoginFailedException.class, () -> {
            filter.unsuccessfulAuthentication(request, response, exception);
        });
    }
}
