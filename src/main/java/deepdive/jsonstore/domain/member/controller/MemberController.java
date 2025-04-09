package deepdive.jsonstore.domain.member.controller;

import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.dto.UpdateMemberRequestDTO;
import deepdive.jsonstore.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/pwReset")
    public ResponseEntity<?> resetPassword(String email, @RequestBody ResetPasswordRequestDTO request) {

        memberService.resetPW(email, request);

        return ResponseEntity.noContent().build();

    }

    @PutMapping("/user")
    public ResponseEntity<?> updateMember(String email, @RequestBody UpdateMemberRequestDTO request) {
        memberService.updateMember(email, request);

        return ResponseEntity.noContent().build();
    }
}
