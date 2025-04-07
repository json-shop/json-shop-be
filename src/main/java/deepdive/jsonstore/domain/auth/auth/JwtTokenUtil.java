package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    public JwtTokenDto generateToken(Authentication authentication, Key key) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + 86400000); // 24시간

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtTokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .build();
    }

    public Claims parseClaims(String accessToken, Key key) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public boolean validateToken(String token, Key key) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
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
}