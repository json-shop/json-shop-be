package deepdive.jsonstore.domain.member.controller;

import deepdive.jsonstore.domain.member.dto.JoinResponse;
import deepdive.jsonstore.domain.member.service.JoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@Valid @RequestBody JoinResponse joinResponse) {
        joinService.joinProcess(joinResponse);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
