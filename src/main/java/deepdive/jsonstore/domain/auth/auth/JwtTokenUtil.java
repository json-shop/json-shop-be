package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.entity.AdminMemberDetails;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenUtil {

    private final long validityInMilliseconds = 1000L * 60 * 60 * 24 * 30; // 24시간

    /**
     * JWT 서명용 키 생성 (HMAC-SHA256)
     */
    public Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * 인증 객체 기반으로 JWT Access Token 생성
     */
    public JwtTokenDto generateToken(UUID adminUid, String authorities, Key key) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + validityInMilliseconds);

        String accessToken = Jwts.builder()
                .setSubject(adminUid.toString())  // UUID를 String으로 변환하여 subject에 포함
                .claim("auth", authorities)  // 권한 정보를 claim으로 포함
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtTokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .build();
    }
    /**
     * JWT Claims 파싱
     */
    public Claims parseClaims(String token, Key key) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            return e.getClaims(); // 만료됐지만 Claims 반환
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new AuthException.InvalidTokenException();
        }
    }

    /**
     * JWT 유효성 검사
     */
    public boolean validateToken(String token, Key key) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature or malformed: {}", e.getMessage());
            throw new AuthException.InvalidTokenException();
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw new AuthException.ExpiredTokenException();
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new AuthException.UnsupportedTokenException();
        } catch (IllegalArgumentException e) {
            log.error("JWT token is empty: {}", e.getMessage());
            throw new AuthException.EmptyTokenException();
        }
    }
}
