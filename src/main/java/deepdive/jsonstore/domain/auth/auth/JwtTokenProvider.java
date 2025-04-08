package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.service.AdminMemberDetailsService;
import deepdive.jsonstore.domain.auth.service.CustomMemberDetailsService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${spring.jwt.secret-key}")
    private String secretKey;

    private final AuthenticationManager authenticationManager;
    private final CustomMemberDetailsService customMemberDetailsService;
    private final AdminMemberDetailsService adminMemberDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final MemberTypeUtil memberTypeUtil;
    private Key key;

    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public JwtTokenDto authenticateAndGenerateToken(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        return jwtTokenUtil.generateToken(authentication, key);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtTokenUtil.parseClaims(token, key);
        String email = claims.getSubject();

        if (memberTypeUtil.isAdmin(email)) {
            return new UsernamePasswordAuthenticationToken(
                    email, "", adminMemberDetailsService.loadUserByUsername(email).getAuthorities()
            );
        } else if (memberTypeUtil.isMember(email)) {
            return new UsernamePasswordAuthenticationToken(
                    email, "", customMemberDetailsService.loadUserByUsername(email).getAuthorities()
            );
        } else {
            throw new UsernameNotFoundException("Email not found: " + email);
        }
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }

    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token, key);
    }
}