package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.auth.service.CustomMemberDetailsService;
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
public class MemberJwtTokenProvider {

    @Value("${spring.jwt.secret-key}")
    private String secretKey;

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomMemberDetailsService customMemberDetailsService;

    private Key key;

    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.key = jwtTokenUtil.getSigningKey(secretKey);
    }

    // 토큰 생성
    public JwtTokenDto generateToken(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();

        // 권한 정보를 String으로 변환
        String authorities = memberDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // UUID와 권한만 포함하여 JWT 토큰 생성
        return jwtTokenUtil.generateToken(memberDetails.getUid(), authorities, key);
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token, key);
    }

    // 토큰에서 Claim 추출
    public Claims parseClaims(String token) {
        return jwtTokenUtil.parseClaims(token, key);
    }

    // 요청 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }

    // 토큰 기반 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        UUID uuid = UUID.fromString(claims.getSubject());
        CustomMemberDetails customMemberDetails = customMemberDetailsService.loadUserByUuid(uuid);  // UUID로 사용자 로드
        return new UsernamePasswordAuthenticationToken(customMemberDetails, "", customMemberDetails.getAuthorities());  // 인증 객체 반환
    }
}
