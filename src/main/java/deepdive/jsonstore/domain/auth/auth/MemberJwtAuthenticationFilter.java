package deepdive.jsonstore.domain.auth.auth;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = memberJwtTokenProvider.resolveToken(request);

        if (!StringUtils.hasText(token)) {
            throw new AuthException.UnauthenticatedAccessException();  // 토큰 없으면 예외 발생
        }

        if (!memberJwtTokenProvider.validateToken(token)) {
            throw new AuthException.UnauthenticatedAccessException();  // 유효하지 않은 토큰도 막기
        }

        Authentication authentication = memberJwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);  // 인증 성공 시에만 다음 필터 진행
    }
}
