package deepdive.jsonstore.domain.auth.controller;

import deepdive.jsonstore.domain.auth.dto.JwtTokenDto;
import deepdive.jsonstore.domain.auth.dto.LoginResponse;
import deepdive.jsonstore.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@RequestBody LoginResponse loginResponse) {
        JwtTokenDto token = authService.adminLogin(loginResponse);
        return ResponseEntity.ok(token);
    }
}