package deepdive.jsonstore.domain.auth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

public class AdminLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AdminJwtTokenProvider adminJwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminLoginAuthenticationFilter(AuthenticationManager authenticationManager, AdminJwtTokenProvider adminJwtTokenProvider) {
        super(new AntPathRequestMatcher("/api/v1/admin/login", "POST"));
        setAuthenticationManager(authenticationManager);
        this.adminJwtTokenProvider = adminJwtTokenProvider;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        return getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {

        JwtTokenDto tokenDto = adminJwtTokenProvider.generateToken(authResult);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(tokenDto));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        // 로그 예외 메시지
        logger.error("Authentication failed for user: " + failed.getMessage());

        // 예외를 던져 GlobalExceptionHandler에서 처리
        throw new AuthException.AdminLoginFailedException();
    }
}