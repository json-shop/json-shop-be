package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final MemberRepository memberRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    private Key key;

    @Value("${jwt.secret}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtTokenDto generateToken(Authentication authentication) {
        return jwtTokenUtil.generateToken(authentication, key);
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = jwtTokenUtil.parseClaims(accessToken, key);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(AuthException.UserNotFoundException::new);

        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member, authorities);
        return new UsernamePasswordAuthenticationToken(customMemberDetails, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            return jwtTokenUtil.validateToken(token, key);
        } catch (SecurityException | MalformedJwtException e) {
            throw new AuthException.InvalidTokenException();
        } catch (ExpiredJwtException e) {
            throw new AuthException.ExpiredTokenException();
        } catch (UnsupportedJwtException e) {
            throw new AuthException.UnsupportedTokenException();
        } catch (IllegalArgumentException e) {
            throw new AuthException.EmptyTokenException();
        }
    }

    public JwtTokenDto authenticateAndGenerateToken(String email, String password) {
        try {
            // 이메일, 비밀번호로 인증
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(email, password);

            Authentication authentication = authenticationManager.authenticate(token);

            // 인증되면 JWT 발급
            return generateToken(authentication);

        } catch (Exception e) {
            throw new AuthException.InvalidCredentialsException();
        }
    }
}