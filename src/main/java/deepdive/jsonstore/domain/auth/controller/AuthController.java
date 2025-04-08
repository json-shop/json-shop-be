package deepdive.jsonstore.domain.auth.controller;

import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginResponse;
import deepdive.jsonstore.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@RequestBody LoginResponse loginResponse) {
        JwtTokenDto tokenDto = authService.login(loginResponse);
        return ResponseEntity.ok(tokenDto);
    }
}
