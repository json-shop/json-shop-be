package deepdive.jsonstore.domain.auth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.common.dto.ErrorResponse;
import deepdive.jsonstore.common.exception.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class MemberJwtAuthenticationFilter extends OncePerRequestFilter {

    private final MemberJwtTokenProvider memberJwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 관리자 API일 경우 멤버 필터는 스킵
        if (requestURI.startsWith("/api/v1/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = memberJwtTokenProvider.resolveToken(request);

            if (!StringUtils.hasText(token)) {
                throw new AuthException.EmptyTokenException(); // 토큰이 비어있는 경우
            }

            if (!memberJwtTokenProvider.validateToken(token)) {
                throw new AuthException.InvalidTokenException(); // 유효하지 않은 토큰
            }

            // 토큰이 유효한 경우 Authentication 설정
            Authentication authentication = memberJwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (AuthException e) {
            handleAuthException(response, e);
        }
    }

    private void handleAuthException(HttpServletResponse response, AuthException e) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode().name(),
                e.getErrorCode().getMessage()
        );

        response.setStatus(e.getErrorCode().getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}