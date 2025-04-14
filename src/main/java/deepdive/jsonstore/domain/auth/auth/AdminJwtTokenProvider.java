package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.entity.AdminMemberDetails;
import deepdive.jsonstore.domain.auth.service.AdminMemberDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminJwtTokenProvider {

    @Value("${spring.jwt.secret-key}")
    private String secretKey;

    private final JwtTokenUtil jwtTokenUtil;
    private final AdminMemberDetailsService adminMemberDetailsService;

    private Key key;

    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.key = jwtTokenUtil.getSigningKey(secretKey);
    }

    // UUID와 권한만 포함된 토큰 생성
    public JwtTokenDto generateToken(Authentication authentication) {
        AdminMemberDetails adminDetails = (AdminMemberDetails) authentication.getPrincipal();
        String authorities = adminDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        // JWT 토큰 생성 시 UUID와 권한만 포함
        return jwtTokenUtil.generateToken(adminDetails.getAdminUid(), authorities, key);
    }

    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token, key);
    }

    public Claims parseClaims(String token) {
        return jwtTokenUtil.parseClaims(token, key);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }

    // UUID와 권한을 사용해 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        UUID adminUid = UUID.fromString(claims.getSubject());  // 토큰에서 UUID 추출
        AdminMemberDetails adminDetails = adminMemberDetailsService.loadUserByUuid(adminUid);  // UUID로 사용자 로드
        return new UsernamePasswordAuthenticationToken(adminDetails, "", adminDetails.getAuthorities());
    }
}
