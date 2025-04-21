package deepdive.jsonstore.domain.admin.controller;

import deepdive.jsonstore.domain.admin.dto.AdminJoinRequest;
import deepdive.jsonstore.domain.admin.service.AdminJoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminJoinController {

    private final AdminJoinService adminJoinService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@Valid @RequestBody AdminJoinRequest adminJoinRequest) {
        log.info("회원가입 요청: {}", adminJoinRequest.toString());
        adminJoinService.adminJoinProcess(adminJoinRequest);
        return ResponseEntity.ok("관리자 회원가입이 완료되었습니다.");
    }
}
