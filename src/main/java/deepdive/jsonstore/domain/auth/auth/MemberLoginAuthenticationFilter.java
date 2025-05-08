package deepdive.jsonstore.domain.auth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.common.dto.ErrorResponse;
import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginRequest;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Slf4j
public class MemberLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final MemberJwtTokenProvider memberJwtTokenProvider;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MemberLoginAuthenticationFilter(AuthenticationManager authenticationManager,
                                           MemberJwtTokenProvider memberJwtTokenProvider,
                                           MeterRegistry meterRegistry) {
        super(new AntPathRequestMatcher("/api/v1/login", "POST"));
        setAuthenticationManager(authenticationManager);
        this.memberJwtTokenProvider = memberJwtTokenProvider;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws IOException {
        LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        log.info("loginRequest email = {}, password = {}", loginRequest.getEmail(), loginRequest.getPassword());

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        return getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {

        JwtTokenDto tokenDto = memberJwtTokenProvider.generateToken(authResult);
        log.info("login success tokenDto = {}", tokenDto.getAccessToken());

        meterRegistry.counter("business.member.login.success").increment();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(tokenDto));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              org.springframework.security.core.AuthenticationException failed)
            throws IOException {

        // 에러 응답 생성
        ErrorResponse errorResponse = new ErrorResponse(
                "MEMBER_LOGIN_FAILED", // 에러 코드
                "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요." // 에러 메시지
        );

        meterRegistry.counter("business.member.login.failure").increment();

        // HTTP 상태와 JSON 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}