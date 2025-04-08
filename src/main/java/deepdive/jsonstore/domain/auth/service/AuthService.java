package deepdive.jsonstore.domain.auth.service;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.auth.JwtTokenProvider;
import deepdive.jsonstore.domain.auth.auth.MemberTypeUtil;
import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberTypeUtil memberTypeUtil;

    public JwtTokenDto login(LoginResponse loginResponse) {
        try {
            return jwtTokenProvider.authenticateAndGenerateToken(loginResponse.getEmail(), loginResponse.getPassword());
        } catch (AuthException.InvalidCredentialsException e) {
            throw new AuthException.InvalidCredentialsException();
        }
    }

    public JwtTokenDto adminLogin(LoginResponse loginResponse) {
        try {
            if (!memberTypeUtil.isAdmin(loginResponse.getEmail())) {
                throw new AuthException.AdminLoginFailedException();
            }
            return jwtTokenProvider.authenticateAndGenerateToken(loginResponse.getEmail(), loginResponse.getPassword());
        } catch (AuthException.InvalidCredentialsException e) {
            throw new AuthException.InvalidCredentialsException();
        }
    }
}