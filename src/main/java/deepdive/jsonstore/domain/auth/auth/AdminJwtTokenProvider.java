package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.service.AdminMemberDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;

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

    public JwtTokenDto generateToken(Authentication authentication) {
        return jwtTokenUtil.generateToken(authentication, key);
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

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String email = claims.getSubject();
        UserDetails userDetails = adminMemberDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
