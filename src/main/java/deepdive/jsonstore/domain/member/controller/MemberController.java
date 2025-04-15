package deepdive.jsonstore.domain.member.controller;

import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.dto.UpdateMemberRequestDTO;
import deepdive.jsonstore.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/pwReset")
    public ResponseEntity<?> resetPassword(@AuthenticationPrincipal(expression = "uid") UUID memberUid, @RequestBody ResetPasswordRequestDTO request) {

        memberService.resetPW(memberUid, request);

        return ResponseEntity.noContent().build();

    }

    @PutMapping("/user")
    public ResponseEntity<?> updateMember(@AuthenticationPrincipal(expression = "uid") UUID memberUid , @RequestBody UpdateMemberRequestDTO request) {
        memberService.updateMember(memberUid, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteMyAccount() {
        memberService.deleteCurrentMember();
        return ResponseEntity.noContent().build();
    }
}
