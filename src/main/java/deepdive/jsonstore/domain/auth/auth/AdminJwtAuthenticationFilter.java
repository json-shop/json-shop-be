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
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final AdminJwtTokenProvider adminJwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 관리자 API가 아닌 경우 필터 통과
        if (!requestURI.startsWith("/api/v1/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = adminJwtTokenProvider.resolveToken(request);

        // 토큰 없으면 그냥 다음 필터로 넘김 (예외 X)
        if (!StringUtils.hasText(token) || !adminJwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = adminJwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
