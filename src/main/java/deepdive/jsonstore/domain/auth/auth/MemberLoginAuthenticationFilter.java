package deepdive.jsonstore.domain.auth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

public class MemberLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationManager authenticationManager;
    private final MemberJwtTokenProvider memberJwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MemberLoginAuthenticationFilter(AuthenticationManager authenticationManager, MemberJwtTokenProvider memberJwtTokenProvider) {
        super(new AntPathRequestMatcher("/api/v1/login", "POST")); // 요청 경로 설정
        this.authenticationManager = authenticationManager;
        this.memberJwtTokenProvider = memberJwtTokenProvider;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        return authenticationManager.authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {

        JwtTokenDto tokenDto = memberJwtTokenProvider.generateToken(authResult);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(tokenDto));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              org.springframework.security.core.AuthenticationException failed)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Login failed\"}");
    }
}
