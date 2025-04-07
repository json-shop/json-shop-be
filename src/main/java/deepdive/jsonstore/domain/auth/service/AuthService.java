package deepdive.jsonstore.domain.auth.service;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.auth.JwtTokenProvider;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenDto login(LoginResponse loginResponse) {
        try {
            // 이메일, 비밀번호로 인증
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(loginResponse.getEmail(), loginResponse.getPassword());

            Authentication authentication = authenticationManager.authenticate(token);

            // 인증되면 JWT 발급
            return jwtTokenProvider.generateToken(authentication);

        } catch (Exception e) {
            throw new AuthException.InvalidCredentialsException();
        }
    }
}
