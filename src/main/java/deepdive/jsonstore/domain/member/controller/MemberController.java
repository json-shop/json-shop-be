package deepdive.jsonstore.domain.member.controller;

import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/api/v1/pwReset")
    public ResponseEntity<?> resetPassword(String email, @RequestBody ResetPasswordRequestDTO request) {

        memberService.resetPW(email, request);

        return ResponseEntity.noContent().build();

    }
}
