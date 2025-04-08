package deepdive.jsonstore.domain.auth.service;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.auth.JwtTokenProvider;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenDto login(LoginResponse loginResponse) {
        try {
            return jwtTokenProvider.authenticateAndGenerateToken(loginResponse.getEmail(), loginResponse.getPassword());
        } catch (AuthException.InvalidCredentialsException e) {
            throw new AuthException.InvalidCredentialsException();
        }
    }
}